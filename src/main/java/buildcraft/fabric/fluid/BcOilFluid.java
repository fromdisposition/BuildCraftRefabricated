package buildcraft.fabric.fluid;

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

public abstract class BcOilFluid extends FlowingFluid {
    protected final Holder holder;

    protected BcOilFluid(Holder holder) {
        this.holder = holder;
    }

    @Override
    public Fluid getFlowing() {
        return holder.flowing;
    }

    @Override
    public Fluid getSource() {
        return holder.still;
    }

    @Override
    public Item getBucket() {
        return holder.bucket;
    }

    @Override
    protected boolean canConvertToSource(ServerLevel level) {
        return false;
    }

    @Override
    protected void beforeDestroyingBlock(LevelAccessor level, BlockPos pos, BlockState state) {
        BlockPos below = pos.below();
        BlockState belowState = level.getBlockState(below);
        if (belowState.liquid()) {
            level.destroyBlock(below, true);
        }
    }

    @Override
    public int getSlopeFindDistance(LevelReader level) {
        return holder.slopeFindDistance;
    }

    @Override
    public int getDropOff(LevelReader level) {
        return holder.dropOff;
    }

    @Override
    public int getTickDelay(LevelReader level) {
        return holder.tickDelay;
    }

    @Override
    public BlockState createLegacyBlock(FluidState state) {
        return holder.block.defaultBlockState()
                .setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
    }

    @Override
    public boolean isSame(Fluid fluid) {
        return fluid == holder.still || fluid == holder.flowing;
    }

    @Override
    protected float getExplosionResistance() {
        return 100.0F;
    }

    @Override
    public boolean canBeReplacedWith(
            FluidState state,
            BlockGetter level,
            BlockPos pos,
            Fluid fluid,
            Direction direction) {
        if (fluid.is(FluidTags.WATER)) {
            return false;
        }
        return fluid == holder.still || fluid == holder.flowing;
    }

    @Override
    public java.util.Optional<net.minecraft.sounds.SoundEvent> getPickupSound() {
        return java.util.Optional.of(SoundEvents.BUCKET_FILL);
    }

    @Override
    public void tick(ServerLevel level, BlockPos pos, BlockState state, FluidState fluidState) {
        if (holder.denseFluid) {
            BlockPos below = pos.below();
            if (level.getFluidState(below).is(FluidTags.WATER)) {
                level.setBlockAndUpdate(below, Blocks.AIR.defaultBlockState());
            }
        }
        super.tick(level, pos, state, fluidState);

        if (!holder.denseFluid && !fluidState.isSource()) {
            FluidState belowFluid = level.getFluidState(pos.below());
            if (belowFluid.is(FluidTags.WATER) && !belowFluid.getType().isSame(this)) {
                FluidState currentFluid = state.getFluidState();
                if (!currentFluid.isEmpty() && currentFluid.getType().isSame(this)) {
                    int neighborAmount = currentFluid.getAmount() - getDropOff(level);
                    if (currentFluid.getValue(FALLING)) {
                        neighborAmount = 7;
                    }
                    if (neighborAmount > 0) {
                        for (Map.Entry<Direction, FluidState> entry : getSpread(level, pos, state).entrySet()) {
                            Direction dir = entry.getKey();
                            spreadTo(level, pos.relative(dir), level.getBlockState(pos.relative(dir)), dir, entry.getValue());
                        }
                    }
                }
            }
        }
    }

    @Override
    protected Map<Direction, FluidState> getSpread(ServerLevel level, BlockPos pos, BlockState state) {
        Map<Direction, FluidState> map = new EnumMap<>(super.getSpread(level, pos, state));
        if (!holder.denseFluid && level.getFluidState(pos.below()).is(FluidTags.WATER)) {
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                if (!map.containsKey(dir)) {
                    BlockPos targetPos = pos.relative(dir);
                    BlockState targetState = level.getBlockState(targetPos);
                    if (targetState.isAir() || targetState.canBeReplaced()) {
                        FluidState targetFluidState = targetState.getFluidState();
                        if (targetFluidState.getType().isSame(this)) {
                            continue;
                        }
                        FluidState newFluid = getNewLiquid(level, targetPos, targetState);
                        if (!newFluid.isEmpty()
                                && targetFluidState.canBeReplacedWith(level, targetPos, newFluid.getType(), dir)) {
                            map.put(dir, newFluid);
                        }
                    }
                }
            }
        }
        return map;
    }

    public static final class Holder {
        public String baseName = "";
        public boolean denseFluid;
        public boolean gaseous;

        public boolean sticky;

        public boolean flammable;
        public int viscosity = 1000;
        public int density = 1000;
        public Fluid still;
        public Fluid flowing;
        public Block block;
        public Item bucket;
        public int tickDelay = 20;
        public int slopeFindDistance = 4;
        public int dropOff = 1;
    }

    public static final class Source extends BcOilFluid {
        public Source(Holder holder) {
            super(holder);
        }

        @Override
        public int getAmount(FluidState state) {
            return 8;
        }

        @Override
        public boolean isSource(FluidState state) {
            return true;
        }
    }

    public static final class Flowing extends BcOilFluid {
        public Flowing(Holder holder) {
            super(holder);
        }

        @Override
        protected void createFluidStateDefinition(
                net.minecraft.world.level.block.state.StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getAmount(FluidState state) {
            return state.getValue(LEVEL);
        }

        @Override
        public boolean isSource(FluidState state) {
            return false;
        }
    }
}

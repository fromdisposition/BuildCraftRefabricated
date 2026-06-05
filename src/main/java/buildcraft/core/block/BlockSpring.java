package buildcraft.core.block;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import buildcraft.api.enums.EnumSpring;

import buildcraft.fabric.BCEnergyFluidsFabric;

public class BlockSpring extends Block implements EntityBlock {

    @FunctionalInterface
    public interface OilTileFactory {
        BlockEntity create(BlockPos pos, BlockState state);
    }

    public static @Nullable OilTileFactory oilTileFactory;

    private final EnumSpring springType;

    public BlockSpring(EnumSpring springType, BlockBehaviour.Properties properties) {
        super(properties
                .strength(-1.0F, 3600000.0F)
                .sound(SoundType.STONE)
                .randomTicks()
        );
        this.springType = springType;
    }

    public EnumSpring getSpringType() {
        return springType;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        generateSpringBlock(level, pos, random);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        level.scheduleTick(pos, this, springType.tickRate);
        if (!level.isClientSide() && level instanceof ServerLevel server) {

            tryPlaceSpringLiquid(server, pos, server.getRandom(), true);
            server.scheduleTick(pos, this, 1);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        generateSpringBlock(level, pos, random);
    }

    private void generateSpringBlock(ServerLevel level, BlockPos pos, RandomSource random) {
        level.scheduleTick(pos, this, springType.tickRate);
        tryPlaceSpringLiquid(level, pos, random, false);
    }

    private void tryPlaceSpringLiquid(ServerLevel level, BlockPos pos, RandomSource random, boolean ignoreChance) {
        BlockState liquidBlock = resolveLiquidBlock(level);
        if (!springType.canGen || liquidBlock == null) {
            return;
        }

        BlockPos upPos = pos.above();
        BlockState upState = level.getBlockState(upPos);
        boolean canPlace = upState.isAir()
                || (!upState.equals(liquidBlock) && hasFluid(level, upPos));
        if (!canPlace) {
            return;
        }

        if (!ignoreChance && springType.chance != -1 && random.nextInt(springType.chance) != 0) {
            return;
        }

        level.setBlock(upPos, liquidBlock, 3);
    }

    private BlockState resolveLiquidBlock(ServerLevel level) {
        if (springType == EnumSpring.OIL) {
            BlockState netherAware = BCEnergyFluidsFabric.oilSourceBlockStateForLevel(level);
            if (netherAware != null) {
                return netherAware;
            }
        }
        return springType.liquidBlock;
    }

    private static boolean hasFluid(Level level, BlockPos pos) {
        FluidState fluidState = level.getFluidState(pos);
        if (!fluidState.isEmpty()) {
            return true;
        }
        Block block = level.getBlockState(pos).getBlock();
        if (block instanceof LiquidBlock) {
            FluidState defaultFluid = block.defaultBlockState().getFluidState();
            return !defaultFluid.isEmpty() && defaultFluid.getType() != Fluids.EMPTY;
        }
        return false;
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (springType == EnumSpring.OIL && oilTileFactory != null) {
            return oilTileFactory.create(pos, state);
        }
        return null;
    }
}

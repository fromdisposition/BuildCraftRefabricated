package buildcraft.fabric.fluid;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class BcOilFluid extends FlowingFluid implements BcFluidPhysicsHost {
   protected final BcOilFluid.Holder holder;
   private Map<Direction, FluidState> spreadCache;

   protected BcOilFluid(BcOilFluid.Holder holder) {
      this.holder = holder;
   }

   @Override
   public BcOilFluid.Holder holder() {
      return this.holder;
   }

   @Override
   public FlowingFluid self() {
      return this;
   }

   int spreadDelay(ServerLevel level, BlockPos pos, FluidState oldState, FluidState newState) {
      return super.getSpreadDelay(level, pos, oldState, newState);
   }

   private void clearSpreadCache() {
      this.spreadCache = null;
   }

   public Fluid getFlowing() {
      return this.holder.flowing;
   }

   public Fluid getSource() {
      return this.holder.still;
   }

   public Item getBucket() {
      return this.holder.bucket;
   }

   protected boolean canConvertToSource(ServerLevel level) {
      return false;
   }

   protected void beforeDestroyingBlock(LevelAccessor level, BlockPos pos, BlockState state) {
      BlockPos adjacent = this.holder.props.gaseous() ? pos.above() : pos.below();
      FluidState adjacentFluid = level.getFluidState(adjacent);
      if (!adjacentFluid.isEmpty() && adjacentFluid.getType().isSame(this)) {
         level.destroyBlock(adjacent, false);
      }
   }

   public int getSlopeFindDistance(LevelReader level) {
      return this.holder.props.slopeFindDistance();
   }

   @Override
   public int getDropOff(LevelReader level) {
      return this.holder.props.dropOff();
   }

   public int getTickDelay(LevelReader level) {
      return this.holder.props.tickDelay();
   }

   public BlockState createLegacyBlock(FluidState state) {
      return (BlockState)this.holder.block.defaultBlockState().setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
   }

   public boolean isSame(Fluid fluid) {
      return fluid == this.holder.still || fluid == this.holder.flowing;
   }

   protected float getExplosionResistance() {
      return 100.0F;
   }

   public boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid fluid, Direction direction) {
      return !state.isEmpty() && !this.isSame(state.getType()) ? false : this.isSame(fluid);
   }

   public Optional<SoundEvent> getPickupSound() {
      return Optional.of(SoundEvents.BUCKET_FILL);
   }

   protected void entityInside(Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier) {
      BcFluidEntityEffects.apply(this.holder, entity);
   }

   public void tick(ServerLevel level, BlockPos pos, BlockState state, FluidState fluidState) {
      this.clearSpreadCache();
      if (this.holder.props.gaseous()) {
         BcGaseousFluidPhysics.tick(this, level, pos, state, fluidState);
      } else {
         BcLiquidFluidPhysics.tickBeforeVanilla(this, level, pos);
         super.tick(level, pos, state, fluidState);
         BcLiquidFluidPhysics.tickAfterVanilla(this, level, pos, state, fluidState);
      }
   }

   protected void spread(ServerLevel level, BlockPos pos, BlockState state, FluidState fluidState) {
      if (this.holder.props.gaseous()) {
         BcGaseousFluidPhysics.spread(this, level, pos, state, fluidState);
      } else {
         super.spread(level, pos, state, fluidState);
      }
   }

   @Override
   public void spreadTo(LevelAccessor level, BlockPos pos, BlockState state, Direction direction, FluidState target) {
      if (!BcLiquidFluidPhysics.displacesWaterAt(this, state, level, pos, target)) {
         super.spreadTo(level, pos, state, direction, target);
      }
   }

   @Override
   public Map<Direction, FluidState> getSpread(ServerLevel level, BlockPos pos, BlockState state) {
      if (this.spreadCache != null) {
         return this.spreadCache;
      }

      Map<Direction, FluidState> result;
      if (this.holder.props.gaseous()) {
         result = BcGaseousFluidPhysics.getSpread(this, level, pos, state);
      } else {
         Map<Direction, FluidState> map = new EnumMap<>(super.getSpread(level, pos, state));
         result = BcLiquidFluidPhysics.enhanceSpread(this, level, pos, state, map);
      }

      this.spreadCache = result;
      return result;
   }

   @Override
   public FluidState getNewLiquid(ServerLevel level, BlockPos pos, BlockState state) {
      return this.holder.props.gaseous() ? BcGaseousFluidPhysics.getNewLiquid(this, level, pos, state) : super.getNewLiquid(level, pos, state);
   }

   public float getHeight(FluidState fluidState, BlockGetter level, BlockPos pos) {
      return this.holder.props.gaseous() ? BcGaseousFluidPhysics.getHeight(fluidState, level, pos) : super.getHeight(fluidState, level, pos);
   }

   public VoxelShape getShape(FluidState state, BlockGetter level, BlockPos pos) {
      return this.holder.props.gaseous() ? BcGaseousFluidPhysics.getShape(state, level, pos, this) : super.getShape(state, level, pos);
   }

   public Vec3 getFlow(BlockGetter level, BlockPos pos, FluidState fluidState) {
      if (this.holder.props.gaseous()) {
         return BcGaseousFluidPhysics.getFlow(level, pos, fluidState);
      }

      Vec3 flow = super.getFlow(level, pos, fluidState);
      if (this.holder.props.viscosity() > 1000) {
         flow = flow.scale(1000.0 / this.holder.props.viscosity());
      }

      return flow;
   }

   public static final class Flowing extends BcOilFluid {
      public Flowing(BcOilFluid.Holder holder) {
         super(holder);
      }

      protected void createFluidStateDefinition(Builder<Fluid, FluidState> builder) {
         super.createFluidStateDefinition(builder);
         builder.add(new Property[]{LEVEL});
      }

      public int getAmount(FluidState state) {
         return (Integer)state.getValue(LEVEL);
      }

      public boolean isSource(FluidState state) {
         return false;
      }
   }

   public static final class Holder {
      public final BcFluidWorldProperties props;
      public final String regName;
      public Fluid still;
      public Fluid flowing;
      public Block block;
      public Item bucket;
      private boolean sealed;

      public Holder(BcFluidWorldProperties props) {
         this.props = props;
         this.regName = BcFluidWorldProperties.regName(props.baseName(), props.heat());
      }

      public void seal() {
         if (this.sealed) {
            throw new IllegalStateException("Holder already sealed: " + this.regName);
         }

         Objects.requireNonNull(this.still, "still");
         Objects.requireNonNull(this.flowing, "flowing");
         Objects.requireNonNull(this.block, "block");
         Objects.requireNonNull(this.bucket, "bucket");
         this.sealed = true;
      }
   }

   public static final class Source extends BcOilFluid {
      public Source(BcOilFluid.Holder holder) {
         super(holder);
      }

      public int getAmount(FluidState state) {
         return 8;
      }

      public boolean isSource(FluidState state) {
         return true;
      }
   }
}

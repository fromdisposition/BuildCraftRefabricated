package buildcraft.lib.fluids;

import buildcraft.lib.common.SoundAction;
import buildcraft.lib.common.SoundActions;
import buildcraft.lib.misc.FluidUtilBC;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jspecify.annotations.Nullable;

public class FluidType {
   public static final int BUCKET_VOLUME = 1000;
   public static final FluidType EMPTY = new FluidType(Fluids.EMPTY, 1000, 1000);
   private static final int WATER_VISCOSITY = 1000;
   private static final int LAVA_VISCOSITY = 3000;
   private final Fluid fluid;
   private final String descriptionId;
   private final int viscosity;
   private final int density;
   private final Map<SoundAction, SoundEvent> sounds = new ConcurrentHashMap<>();

   public FluidType(Fluid fluid) {
      this(fluid, defaultViscosity(fluid), defaultDensity(fluid));
   }

   public FluidType(Fluid fluid, int viscosity, int density) {
      this.fluid = fluid;
      this.viscosity = viscosity;
      this.density = density;
      Identifier key = BuiltInRegistries.FLUID.getKey(FluidUtilBC.canonicalFluid(fluid));
      this.descriptionId = key == null ? "fluid_type.minecraft.empty" : key.toLanguageKey("fluid_type");
      if (fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER) {
         this.sounds.put(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL);
         this.sounds.put(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY);
      } else if (fluid == Fluids.LAVA || fluid == Fluids.FLOWING_LAVA) {
         this.sounds.put(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL_LAVA);
         this.sounds.put(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_LAVA);
      }
   }

   private static int defaultViscosity(Fluid fluid) {
      return fluid != Fluids.LAVA && fluid != Fluids.FLOWING_LAVA ? 1000 : 3000;
   }

   private static int defaultDensity(Fluid fluid) {
      return 1000;
   }

   public Fluid getFluid() {
      return this.fluid;
   }

   public int getViscosity() {
      return this.viscosity;
   }

   public int getViscosity(FluidStack stack) {
      return this.getViscosity();
   }

   public int getViscosity(FluidState state, BlockAndLightGetter getter, BlockPos pos) {
      return this.getViscosity();
   }

   public int getDensity() {
      return this.density;
   }

   public final boolean isLighterThanAir() {
      return this.density <= 0;
   }

   public String getDescriptionId() {
      return this.descriptionId;
   }

   public String getDescriptionId(FluidStack stack) {
      return this.getDescriptionId();
   }

   public Component getDescription(FluidStack stack) {
      return Component.translatable(this.getDescriptionId());
   }

   public @Nullable SoundEvent getSound(SoundAction action) {
      return this.sounds.get(action);
   }

   public @Nullable Item getBucket(FluidStack stack) {
      return this.fluid.getBucket();
   }

   public boolean canBePlacedInLevel(Level level, BlockPos pos, FluidStack stack) {
      return !stack.isEmpty() && this.fluid != Fluids.EMPTY;
   }

   public boolean isVaporizedOnPlacement(Level level, BlockPos pos, FluidStack stack) {
      return this.fluid == Fluids.WATER && level.dimension() == Level.NETHER;
   }

   public void onVaporize(@Nullable Player player, Level level, BlockPos pos, FluidStack stack) {
      level.playSound(player, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F);
   }

   public BlockState getStateForPlacement(Level level, BlockPos pos, FluidStack stack) {
      return this.fluid.defaultFluidState().createLegacyBlock();
   }

   public BlockState getBlockForFluidState(Level level, BlockPos pos, FluidState fluidState) {
      return fluidState.createLegacyBlock();
   }

   @Override
   public String toString() {
      return this.descriptionId;
   }
}

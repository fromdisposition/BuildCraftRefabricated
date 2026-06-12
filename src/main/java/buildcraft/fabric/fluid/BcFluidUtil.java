package buildcraft.fabric.fluid;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

/**
 * BC custom fluid physics helpers. Tags and entity tracking use {@link BcFluidTags} directly;
 * oil heat tiers register via {@link buildcraft.fabric.BCEnergyFluidsFabric}.
 */
public final class BcFluidUtil {
   private BcFluidUtil() {
   }

   /**
    * True only for vanilla water. BC oil/fuel use {@link BcFluidTags#BC_FLUIDS} for
    * entity/render detection — use this helper anywhere oil must not be treated as real water.
    */
   public static boolean isVanillaWater(FluidState state) {
      return !state.isEmpty() && state.getType().isSame(Fluids.WATER);
   }

   public static boolean isEyeInGaseousBcFluid(Entity entity) {
      return entity.isEyeInFluid(BcFluidTags.BC_FLUIDS) && !entity.isEyeInFluid(BcFluidTags.BC_LIQUIDS);
   }
}

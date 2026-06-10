package buildcraft.fabric.fluid;

import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public final class BcFluidUtil {
   private BcFluidUtil() {
   }

   /**
    * True only for vanilla water. BC oil/fuel fluids are also in {@code minecraft:water} for engine compat
    * (ticks, buckets, entity fluid checks) — use this helper anywhere oil must not be treated as water.
    */
   public static boolean isVanillaWater(FluidState state) {
      return !state.isEmpty() && state.getType().isSame(Fluids.WATER);
   }
}

package buildcraft.fabric.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

/**
 * BC fluid helpers shared by world physics and client fog/overlay.
 * Liquids use {@link BcFluidTags#BC_LIQUIDS} via {@link BcFluidEntityInteractions};
 * all variants use {@link BcFluidTags#BC_FLUIDS} for block-level detection.
 */
public final class BcFluidUtil {
   private BcFluidUtil() {
   }

   /** True only for vanilla water — BC fluids must never pass this check. */
   public static boolean isVanillaWater(FluidState state) {
      return !state.isEmpty() && state.getType().isSame(Fluids.WATER);
   }

   /** Block at the entity eye is a BC fluid (liquids and gases). */
   public static boolean isBcFluidAtEye(Entity entity) {
      if (entity.level() == null) {
         return false;
      }

      FluidState state = entity.level().getFluidState(BlockPos.containing(entity.getX(), entity.getEyeY(), entity.getZ()));
      return !state.isEmpty() && state.getType().is(BcFluidTags.BC_FLUIDS);
   }

   /** Entity eye is inside a BC fluid (tracker for liquids, block check for gases and edge cases). */
   public static boolean isEyeInBcFluid(Entity entity) {
      return entity.isEyeInFluid(BcFluidTags.BC_LIQUIDS) || isBcFluidAtEye(entity);
   }
}

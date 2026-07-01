package buildcraft.fabric.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

/**
 * BC fluid helpers shared by world physics and client fog/overlay.
 * Liquids use {@link BcFluidTags#BC_LIQUIDS} via {@link BcFluidEntityInteractions};
 * all variants use {@link BcFluidTags#BC_FLUIDS} for block-level detection.
 */
public final class BcFluidUtil {
   private static final double SUBMERGED_EPSILON = 1.0E-5;

   private BcFluidUtil() {
   }

   /** True only for vanilla water — BC fluids must never pass this check. */
   public static boolean isVanillaWater(FluidState state) {
      return !state.isEmpty() && state.getType().isSame(Fluids.WATER);
   }

   public static boolean isBcFluidState(FluidState state) {
      return !state.isEmpty() && state.is(BcFluidTags.BC_FLUIDS);
   }

   /**
    * Sample point is below the BC fluid surface in its block.
    * Matches vanilla {@code EntityFluidInteraction} height logic for fog and overlay.
    */
   public static boolean isSubmergedInBcFluid(Level level, double x, double sampleY, double z) {
      BlockPos pos = BlockPos.containing(x, sampleY, z);
      FluidState state = level.getFluidState(pos);
      if (!isBcFluidState(state)) {
         return false;
      }

      double surfaceY = pos.getY() + state.getHeight(level, pos);
      return sampleY < surfaceY - SUBMERGED_EPSILON;
   }

   /** Block at the entity eye is below a BC fluid surface (liquids and gases). */
   public static boolean isBcFluidAtEye(Entity entity) {
      if (entity.level() == null) {
         return false;
      }

      return isSubmergedInBcFluid(entity.level(), entity.getX(), entity.getEyeY(), entity.getZ());
   }

   /** Entity eye is inside a BC fluid (tracker for liquids, height check for gases and edge cases). */
   public static boolean isEyeInBcFluid(Entity entity) {
      return entity.isEyeInFluid(BcFluidTags.BC_LIQUIDS) || isBcFluidAtEye(entity);
   }
}

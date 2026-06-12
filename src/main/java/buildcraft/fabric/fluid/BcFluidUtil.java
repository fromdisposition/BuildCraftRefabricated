package buildcraft.fabric.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

/**
 * BC fluid helpers shared by physics mixins and client fog/overlay.
 * Liquids use {@link BcFluidTags#BC_LIQUIDS} for water-like physics; all variants use
 * {@link BcFluidTags#BC_FLUIDS} for detection, fog, and overlay.
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

   /** Entity eye is inside any BC fluid tracker or block (fog, overlay, camera). */
   public static boolean isEyeInBcFluid(Entity entity) {
      return entity.isEyeInFluid(BcFluidTags.BC_FLUIDS)
         || entity.isEyeInFluid(BcFluidTags.BC_LIQUIDS)
         || isBcFluidAtEye(entity);
   }

   public static boolean isEyeInGaseousBcFluid(Entity entity) {
      return entity.isEyeInFluid(BcFluidTags.BC_FLUIDS) && !entity.isEyeInFluid(BcFluidTags.BC_LIQUIDS);
   }
}

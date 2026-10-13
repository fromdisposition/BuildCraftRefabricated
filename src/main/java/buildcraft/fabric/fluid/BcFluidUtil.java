package buildcraft.fabric.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public final class BcFluidUtil {
   private static final double SUBMERGED_EPSILON = 1.0E-5;

   private BcFluidUtil() {
   }

   public static boolean isVanillaWater(FluidState state) {
      return !state.isEmpty() && state.getType().isSame(Fluids.WATER);
   }

   public static boolean isBcFluidState(FluidState state) {
      return !state.isEmpty() && state.is(BcFluidTags.BC_FLUIDS);
   }

   // Same surface test as vanilla's fluid fog and overlay logic.
   public static boolean isSubmergedInBcFluid(Level level, double x, double sampleY, double z) {
      BlockPos pos = BlockPos.containing(x, sampleY, z);
      FluidState state = level.getFluidState(pos);
      if (!isBcFluidState(state)) {
         return false;
      }

      double surfaceY = pos.getY() + state.getHeight(level, pos);
      return sampleY < surfaceY - SUBMERGED_EPSILON;
   }

   public static boolean isBcFluidAtEye(Entity entity) {
      if (entity.level() == null) {
         return false;
      }

      return isSubmergedInBcFluid(entity.level(), entity.getX(), entity.getEyeY(), entity.getZ());
   }
}


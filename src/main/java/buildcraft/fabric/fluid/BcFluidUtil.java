package buildcraft.fabric.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;

/**
 * BC custom fluid physics, tags, and detection. Identity, transfer, and tank logic live in
 * {@link BcFluidTags#BC_FLUIDS}; oil heat tiers register via {@link buildcraft.fabric.BCEnergyFluidsFabric}.
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

   public static boolean isBcFluid(FluidState state) {
      return isBcFluidTag(state);
   }

   public static boolean isBcFluid(Fluid fluid) {
      return fluid != null && !fluid.defaultFluidState().isEmpty() && fluid.is(BcFluidTags.BC_FLUIDS);
   }

   public static boolean isBcFluidTag(FluidState state) {
      return !state.isEmpty() && state.getType().is(BcFluidTags.BC_FLUIDS);
   }

   /** True when any part of the entity fluid-interaction box overlaps BC fluid (incl. shallow wading). */
   public static boolean touchesBcFluid(Entity entity) {
      if (entity.isEyeInFluid(BcFluidTags.BC_FLUIDS)) {
         return true;
      }

      AABB box = entity.getFluidInteractionBox();
      if (box == null) {
         return isBcFluidTag(entity.level().getFluidState(entity.blockPosition()));
      }

      Level level = entity.level();
      int x0 = Mth.floor(box.minX);
      int y0 = Mth.floor(box.minY);
      int z0 = Mth.floor(box.minZ);
      int x1 = Mth.ceil(box.maxX) - 1;
      int y1 = Mth.ceil(box.maxY) - 1;
      int z1 = Mth.ceil(box.maxZ) - 1;
      BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

      for (int x = x0; x <= x1; x++) {
         for (int y = y0; y <= y1; y++) {
            for (int z = z0; z <= z1; z++) {
               pos.set(x, y, z);
               FluidState fluidState = level.getFluidState(pos);
               if (isBcFluidTag(fluidState)) {
                  double fluidTop = y + fluidState.getHeight(level, pos);
                  if (fluidTop >= box.minY) {
                     return true;
                  }
               }
            }
         }
      }

      return false;
   }
}

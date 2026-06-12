package buildcraft.fabric.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;

/**
 * BC custom fluid physics, tags, and detection. Identity, transfer, and tank logic live in
 * {@link BcFluidTags}; oil heat tiers register via {@link buildcraft.fabric.BCEnergyFluidsFabric}.
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
      return isTagged(state, BcFluidTags.BC_FLUIDS);
   }

   public static boolean isBcFluid(Fluid fluid) {
      return fluid != null && !fluid.defaultFluidState().isEmpty() && fluid.is(BcFluidTags.BC_FLUIDS);
   }

   public static boolean isBcLiquid(FluidState state) {
      return isTagged(state, BcFluidTags.BC_LIQUIDS);
   }

   public static boolean isBcLiquid(Fluid fluid) {
      return fluid != null && !fluid.defaultFluidState().isEmpty() && fluid.is(BcFluidTags.BC_LIQUIDS);
   }

   /** True when any part of the entity fluid-interaction box overlaps a BC fluid (incl. gases). */
   public static boolean touchesBcFluid(Entity entity) {
      return touchesFluidTag(entity, BcFluidTags.BC_FLUIDS);
   }

   /** True when any part of the entity fluid-interaction box overlaps a non-gaseous BC liquid. */
   public static boolean touchesBcLiquid(Entity entity) {
      return touchesFluidTag(entity, BcFluidTags.BC_LIQUIDS);
   }

   public static boolean shouldCrawlSwimInBcLiquid(LivingEntity entity) {
      return entity.isSprinting() && !entity.isPassenger() && entity.isEyeInFluid(BcFluidTags.BC_LIQUIDS);
   }

   public static boolean isEyeInGaseousBcFluid(Entity entity) {
      return entity.isEyeInFluid(BcFluidTags.BC_FLUIDS) && !entity.isEyeInFluid(BcFluidTags.BC_LIQUIDS);
   }

   public static boolean touchesFluidTag(Entity entity, TagKey<Fluid> tag) {
      if (entity.isEyeInFluid(tag)) {
         return true;
      }

      return intersectsFluidTag(entity, tag);
   }

   private static boolean intersectsFluidTag(Entity entity, TagKey<Fluid> tag) {
      Level level = entity.level();
      AABB box = entity.getFluidInteractionBox();
      if (box == null) {
         return isTagged(level.getFluidState(entity.blockPosition()), tag);
      }

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
               if (isTagged(fluidState, tag)) {
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

   private static boolean isTagged(FluidState state, TagKey<Fluid> tag) {
      return !state.isEmpty() && state.getType().is(tag);
   }
}

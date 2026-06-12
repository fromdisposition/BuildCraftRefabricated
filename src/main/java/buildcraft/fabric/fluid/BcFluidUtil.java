package buildcraft.fabric.fluid;

import buildcraft.fabric.BCEnergyFluidsFabric;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;

/**
 * BC custom fluid physics, tags, and client appearance. Identity, transfer, and tank logic live in
 * {@link buildcraft.lib.fluid.BcFluids}; oil heat tiers register via {@link buildcraft.fabric.BCEnergyFluidsFabric}.
 */
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

   public static boolean isBcFluid(FluidState state) {
      return clientAppearance(state) != null;
   }

   public static boolean isBcFluid(Fluid fluid) {
      return clientAppearance(fluid) != null;
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

   @Nullable
   public static BcFluidClientAppearance clientAppearance(FluidState state) {
      if (state.isEmpty()) {
         return null;
      }

      BCEnergyFluidsFabric.FluidEntry entry = BCEnergyFluidsFabric.findEntry(state.getType());
      return entry == null ? null : entry.holder().props.clientAppearance();
   }

   @Nullable
   public static BcFluidClientAppearance clientAppearance(Fluid fluid) {
      if (fluid == null) {
         return null;
      }

      BCEnergyFluidsFabric.FluidEntry entry = BCEnergyFluidsFabric.findEntry(fluid);
      return entry == null ? null : entry.holder().props.clientAppearance();
   }
}

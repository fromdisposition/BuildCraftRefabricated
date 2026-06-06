package buildcraft.fabric;

import buildcraft.lib.misc.FluidUtilBC;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributeHandler;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.world.level.material.Fluid;

public final class BcFluidAttributesFabric {
   private static final FluidVariantAttributeHandler GASEOUS = new FluidVariantAttributeHandler() {
      public boolean isLighterThanAir(FluidVariant variant) {
         return FluidUtilBC.isGaseous(variant.getFluid());
      }
   };
   private static final FluidVariantAttributeHandler LIQUID = new FluidVariantAttributeHandler() {
      public boolean isLighterThanAir(FluidVariant variant) {
         return false;
      }
   };

   private BcFluidAttributesFabric() {
   }

   public static void register(Fluid still, Fluid flowing, boolean gaseous) {
      FluidVariantAttributeHandler handler = gaseous ? GASEOUS : LIQUID;
      FluidVariantAttributes.register(still, handler);
      FluidVariantAttributes.register(flowing, handler);
   }
}

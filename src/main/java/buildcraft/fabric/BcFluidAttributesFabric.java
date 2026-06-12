package buildcraft.fabric;

import buildcraft.lib.fluid.meta.FluidAttributes;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributeHandler;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;

public final class BcFluidAttributesFabric {
   private static final FluidVariantAttributeHandler GASEOUS = new FluidVariantAttributeHandler() {
      public Component getName(FluidVariant variant) {
         return BcFluidAttributesFabric.displayName(variant);
      }

      public boolean isLighterThanAir(FluidVariant variant) {
         return FluidAttributes.of(variant.getFluid()).isLighterThanAir();
      }
   };
   private static final FluidVariantAttributeHandler LIQUID = new FluidVariantAttributeHandler() {
      public Component getName(FluidVariant variant) {
         return BcFluidAttributesFabric.displayName(variant);
      }

      public boolean isLighterThanAir(FluidVariant variant) {
         return false;
      }
   };

   private static Component displayName(FluidVariant variant) {
      if (variant.isBlank()) {
         return Component.empty();
      }

      return FluidAttributes.displayName(variant.getFluid());
   }

   private BcFluidAttributesFabric() {
   }

   public static void register(Fluid still, Fluid flowing, boolean gaseous) {
      FluidVariantAttributeHandler handler = gaseous ? GASEOUS : LIQUID;
      FluidVariantAttributes.register(still, handler);
      FluidVariantAttributes.register(flowing, handler);
   }
}

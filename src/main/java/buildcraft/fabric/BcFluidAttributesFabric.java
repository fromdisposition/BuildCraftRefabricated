package buildcraft.fabric;

import buildcraft.lib.fluids.FluidTypes;
import buildcraft.lib.misc.FluidUtilBC;
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
         return FluidUtilBC.isGaseous(variant.getFluid());
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

      // Use fluid_type lang keys directly — not FluidUtilBC.getFluidDisplayName(), which calls
      // FluidVariantRendering.getTooltip() and would recurse back into this getName().
      return Component.translatable(FluidTypes.descriptionIdFor(variant.getFluid()));
   }

   private BcFluidAttributesFabric() {
   }

   public static void register(Fluid still, Fluid flowing, boolean gaseous) {
      FluidVariantAttributeHandler handler = gaseous ? GASEOUS : LIQUID;
      FluidVariantAttributes.register(still, handler);
      FluidVariantAttributes.register(flowing, handler);
   }
}

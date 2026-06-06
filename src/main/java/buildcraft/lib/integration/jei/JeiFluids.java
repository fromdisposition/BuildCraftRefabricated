package buildcraft.lib.integration.jei;

import buildcraft.lib.fluids.FluidStack;
import buildcraft.lib.transfer.fabric.TransferConvert;
import java.util.Optional;
import mezz.jei.api.fabric.constants.FabricTypes;
import mezz.jei.api.fabric.ingredients.fluids.JeiFluidIngredient;
import mezz.jei.api.gui.builder.IClickableIngredientFactory;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.runtime.IClickableIngredient;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.renderer.Rect2i;

public final class JeiFluids {
   private JeiFluids() {
   }

   public static void addFluidStack(IRecipeSlotBuilder slot, FluidStack stack) {
      addFluidStack(slot, stack, stack.getAmount());
   }

   public static void addFluidStack(IRecipeSlotBuilder slot, FluidStack stack, long amount) {
      if (stack != null && !stack.isEmpty()) {
         FluidVariant variant = TransferConvert.toVariant(stack);
         slot.add(FabricTypes.FLUID_STACK, new JeiFluidIngredient(variant, amount));
      }
   }

   public static Optional<? extends IClickableIngredient<?>> clickableFluidIngredient(
      IClickableIngredientFactory factory, FluidStack fluid, long amount, int x, int y, int width, int height
   ) {
      if (fluid != null && !fluid.isEmpty() && amount > 0L) {
         FluidVariant variant = TransferConvert.toVariant(fluid);
         return factory.createBuilder(FabricTypes.FLUID_STACK, new JeiFluidIngredient(variant, amount)).buildWithArea(new Rect2i(x, y, width, height));
      } else {
         return Optional.empty();
      }
   }
}

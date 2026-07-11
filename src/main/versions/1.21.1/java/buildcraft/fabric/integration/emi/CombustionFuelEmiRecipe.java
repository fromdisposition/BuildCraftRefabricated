package buildcraft.fabric.integration.emi;

import buildcraft.energy.recipe.CombustionFuelRecipe;
import buildcraft.lib.fluid.stack.FluidStack;
import buildcraft.lib.misc.LocaleUtil;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import java.util.List;
import net.minecraft.network.chat.Component;

class CombustionFuelEmiRecipe extends BcEmiRecipe {
   private final CombustionFuelRecipe recipe;

   CombustionFuelEmiRecipe(EmiRecipeCategory category, CombustionFuelRecipe recipe) {
      super(
         category,
         List.of(BcEmi.fluid(new FluidStack(recipe.fluid(), 1000))),
         recipe.residueFluid() == null ? List.of() : List.of(BcEmi.fluid(new FluidStack(recipe.residueFluid(), recipe.residueAmountPer1000Mb()))),
         130,
         38
      );
      this.recipe = recipe;
   }

   @Override
   public void addWidgets(WidgetHolder widgets) {
      widgets.addSlot(this.getInputs().get(0), 0, 0);
      if (!this.getOutputs().isEmpty()) {
         widgets.addSlot(this.getOutputs().get(0), 0, 18).recipeContext(this);
      }

      widgets.addText(Component.literal(LocaleUtil.localizeMjFlow(this.recipe.powerPerCycle())), 24, 4, BcEmi.TEXT_COLOUR, false);
      String burn = LocaleUtil.localize("gui.jei.category.buildcraft.combustion_engine_fuel.burn", this.recipe.totalBurningTime());
      widgets.addText(Component.literal(burn), 24, 16, BcEmi.TEXT_COLOUR, false);
   }
}

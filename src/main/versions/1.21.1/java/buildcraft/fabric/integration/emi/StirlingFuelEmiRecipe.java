package buildcraft.fabric.integration.emi;

import buildcraft.api.mj.MjAPI;
import buildcraft.energy.integration.jei.StirlingFuelJei;
import buildcraft.lib.misc.LocaleUtil;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import java.util.List;
import net.minecraft.network.chat.Component;

class StirlingFuelEmiRecipe extends BcEmiRecipe {
   private final StirlingFuelJei recipe;

   StirlingFuelEmiRecipe(EmiRecipeCategory category, StirlingFuelJei recipe) {
      super(category, List.of(EmiStack.of(recipe.fuel())), List.of(), 150, 24);
      this.recipe = recipe;
   }

   @Override
   public void addWidgets(WidgetHolder widgets) {
      widgets.addSlot(EmiStack.of(this.recipe.fuel()), 0, 2);

      String rate = LocaleUtil.localize("gui.jei.category.buildcraft.stirling_engine_fuel.rate", LocaleUtil.localizeMjFlow(MjAPI.MJ));
      widgets.addText(Component.literal(rate), 22, 0, BcEmi.TEXT_COLOUR, false);
      String burn = LocaleUtil.localize("gui.jei.category.buildcraft.stirling_engine_fuel.burn", this.recipe.burnTime());
      widgets.addText(Component.literal(burn), 22, 12, BcEmi.TEXT_COLOUR, false);
   }
}

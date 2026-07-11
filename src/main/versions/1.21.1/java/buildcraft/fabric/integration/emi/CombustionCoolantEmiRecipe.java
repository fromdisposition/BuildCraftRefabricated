package buildcraft.fabric.integration.emi;

import buildcraft.energy.integration.jei.CombustionCoolantJei;
import buildcraft.lib.misc.LocaleUtil;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import java.util.List;
import net.minecraft.network.chat.Component;

class CombustionCoolantEmiRecipe extends BcEmiRecipe {
   private final CombustionCoolantJei recipe;

   CombustionCoolantEmiRecipe(EmiRecipeCategory category, CombustionCoolantJei recipe) {
      super(
         category,
         recipe.isSolid()
            ? List.of((EmiIngredient)EmiStack.of(recipe.item()), BcEmi.fluid(recipe.fluid()))
            : List.of((EmiIngredient)BcEmi.fluid(recipe.fluid())),
         List.of(),
         150,
         20
      );
      this.recipe = recipe;
   }

   @Override
   public void addWidgets(WidgetHolder widgets) {
      int x = 0;
      if (this.recipe.isSolid()) {
         widgets.addSlot(EmiStack.of(this.recipe.item()), x, 0);
         x += 18;
      }

      widgets.addSlot(BcEmi.fluid(this.recipe.fluid()), x, 0);

      String info = this.recipe.isSolid()
         ? LocaleUtil.localize("gui.jei.category.buildcraft.combustion_engine_coolant.melts", this.recipe.fluid().getAmount())
         : LocaleUtil.localize("gui.jei.category.buildcraft.combustion_engine_coolant.cooling", String.format("%.4f", this.recipe.coolingPerMb()));
      widgets.addText(Component.literal(info), x + 22, 5, BcEmi.TEXT_COLOUR, false);
   }
}

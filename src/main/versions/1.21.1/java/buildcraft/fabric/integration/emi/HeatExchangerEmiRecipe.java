package buildcraft.fabric.integration.emi;

import buildcraft.factory.integration.jei.HeatExchangerRecipePair;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import java.util.List;

class HeatExchangerEmiRecipe extends BcEmiRecipe {
   private final HeatExchangerRecipePair pair;

   HeatExchangerEmiRecipe(EmiRecipeCategory category, HeatExchangerRecipePair pair) {
      super(
         category,
         List.of((EmiIngredient)BcEmi.fluid(pair.heatable().in()), BcEmi.fluid(pair.coolable().in())),
         List.of(BcEmi.fluid(pair.heatable().out()), BcEmi.fluid(pair.coolable().out())),
         118,
         36
      );
      this.pair = pair;
   }

   @Override
   public void addWidgets(WidgetHolder widgets) {
      // Top row: the heated fluid; bottom row: the coolant giving its heat away.
      widgets.addSlot(BcEmi.fluid(this.pair.heatable().in()), 0, 0);
      widgets.addTexture(EmiTexture.EMPTY_ARROW, 24, 1);
      widgets.addSlot(BcEmi.fluid(this.pair.heatable().out()), 56, 0).recipeContext(this);

      widgets.addSlot(BcEmi.fluid(this.pair.coolable().in()), 0, 18);
      widgets.addTexture(EmiTexture.EMPTY_ARROW, 24, 19);
      widgets.addSlot(BcEmi.fluid(this.pair.coolable().out()), 56, 18).recipeContext(this);
   }
}

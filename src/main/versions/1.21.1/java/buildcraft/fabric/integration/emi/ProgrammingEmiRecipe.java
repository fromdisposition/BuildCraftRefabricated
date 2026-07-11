package buildcraft.fabric.integration.emi;

import buildcraft.lib.misc.LocaleUtil;
import buildcraft.silicon.integration.jei.ProgrammingRecipeJei;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import java.util.List;
import net.minecraft.network.chat.Component;

class ProgrammingEmiRecipe extends BcEmiRecipe {
   private final ProgrammingRecipeJei recipe;

   ProgrammingEmiRecipe(EmiRecipeCategory category, ProgrammingRecipeJei recipe) {
      super(category, List.of(EmiStack.of(recipe.input())), List.of(EmiStack.of(recipe.option())), 118, 30);
      this.recipe = recipe;
   }

   @Override
   public void addWidgets(WidgetHolder widgets) {
      widgets.addSlot(EmiStack.of(this.recipe.input()), 0, 0);
      widgets.addTexture(EmiTexture.EMPTY_ARROW, 24, 1);
      widgets.addSlot(EmiStack.of(this.recipe.option()), 56, 0).recipeContext(this);

      String power = LocaleUtil.localize("gui.jei.category.buildcraft.assembly_table.power", LocaleUtil.localizeMj(this.recipe.microJoules()));
      widgets.addText(Component.literal(power), 0, 20, BcEmi.TEXT_COLOUR, false);
   }
}

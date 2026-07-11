package buildcraft.fabric.integration.emi;

import buildcraft.lib.misc.LocaleUtil;
import buildcraft.silicon.integration.jei.AssemblyRecipeJei;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import java.util.List;
import net.minecraft.network.chat.Component;

class AssemblyEmiRecipe extends BcEmiRecipe {
   private final AssemblyRecipeJei recipe;
   private final int gridRows;

   AssemblyEmiRecipe(EmiRecipeCategory category, AssemblyRecipeJei recipe) {
      super(
         category,
         recipe.inputSlots().stream().map(alternatives -> (EmiIngredient)EmiIngredient.of(alternatives.stream().map(EmiStack::of).toList())).toList(),
         recipe.outputs().stream().map(EmiStack::of).toList(),
         118,
         Math.max(3, (recipe.inputSlots().size() + 2) / 3) * 18 + 12
      );
      this.recipe = recipe;
      this.gridRows = Math.max(1, (recipe.inputSlots().size() + 2) / 3);
   }

   @Override
   public void addWidgets(WidgetHolder widgets) {
      List<EmiIngredient> inputs = this.getInputs();
      for (int i = 0; i < inputs.size(); i++) {
         widgets.addSlot(inputs.get(i), i % 3 * 18, i / 3 * 18);
      }

      int midY = this.gridRows * 18 / 2 - 8;
      widgets.addTexture(EmiTexture.EMPTY_ARROW, 60, midY);
      for (int i = 0; i < this.getOutputs().size(); i++) {
         widgets.addSlot(this.getOutputs().get(i), 92, midY + i * 18).recipeContext(this);
      }

      String power = LocaleUtil.localize("gui.jei.category.buildcraft.assembly_table.power", LocaleUtil.localizeMj(this.recipe.microJoules()));
      widgets.addText(Component.literal(power), 0, this.getDisplayHeight() - 10, BcEmi.TEXT_COLOUR, false);
   }
}

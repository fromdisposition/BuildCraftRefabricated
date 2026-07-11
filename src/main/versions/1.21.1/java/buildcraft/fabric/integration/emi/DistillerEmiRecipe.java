package buildcraft.fabric.integration.emi;

import buildcraft.api.recipes.IRefineryRecipeManager;
import buildcraft.lib.misc.LocaleUtil;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import java.util.List;
import net.minecraft.network.chat.Component;

class DistillerEmiRecipe extends BcEmiRecipe {
   private final IRefineryRecipeManager.IDistillationRecipe recipe;

   DistillerEmiRecipe(EmiRecipeCategory category, IRefineryRecipeManager.IDistillationRecipe recipe) {
      super(
         category,
         List.of(BcEmi.fluid(recipe.in())),
         List.of(BcEmi.fluid(recipe.outGas()), BcEmi.fluid(recipe.outLiquid())),
         118,
         48
      );
      this.recipe = recipe;
   }

   @Override
   public void addWidgets(WidgetHolder widgets) {
      widgets.addSlot(BcEmi.fluid(this.recipe.in()), 0, 9);
      widgets.addTexture(EmiTexture.EMPTY_ARROW, 24, 10);
      // Gas leaves through the top, liquid through the bottom — mirror the machine's layout.
      widgets.addSlot(BcEmi.fluid(this.recipe.outGas()), 56, 0).recipeContext(this);
      widgets.addSlot(BcEmi.fluid(this.recipe.outLiquid()), 56, 18).recipeContext(this);

      String power = LocaleUtil.localize("gui.jei.category.buildcraft.assembly_table.power", LocaleUtil.localizeMj(this.recipe.powerRequired()));
      widgets.addText(Component.literal(power), 0, 38, BcEmi.TEXT_COLOUR, false);
   }
}

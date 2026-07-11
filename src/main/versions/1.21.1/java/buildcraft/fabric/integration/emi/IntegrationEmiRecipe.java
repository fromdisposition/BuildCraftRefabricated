package buildcraft.fabric.integration.emi;

import buildcraft.lib.misc.LocaleUtil;
import buildcraft.silicon.integration.jei.IntegrationRecipeJei;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

class IntegrationEmiRecipe extends BcEmiRecipe {
   /** Ring slot offsets around the centre of a 3x3 grid, clockwise from the top. */
   private static final int[][] RING = {{18, 0}, {36, 0}, {36, 18}, {36, 36}, {18, 36}, {0, 36}, {0, 18}, {0, 0}};

   private final IntegrationRecipeJei recipe;

   IntegrationEmiRecipe(EmiRecipeCategory category, IntegrationRecipeJei recipe) {
      super(category, inputsOf(recipe), List.of(EmiStack.of(recipe.output())), 128, 66);
      this.recipe = recipe;
   }

   private static List<EmiIngredient> inputsOf(IntegrationRecipeJei recipe) {
      List<EmiIngredient> inputs = new ArrayList<>();
      inputs.add(EmiStack.of(recipe.center()));
      for (ItemStack stack : recipe.ring()) {
         if (!stack.isEmpty()) {
            inputs.add(EmiStack.of(stack));
         }
      }

      return inputs;
   }

   @Override
   public void addWidgets(WidgetHolder widgets) {
      widgets.addSlot(EmiStack.of(this.recipe.center()), 18, 18);
      List<ItemStack> ring = this.recipe.ring();
      for (int i = 0; i < ring.size() && i < RING.length; i++) {
         if (!ring.get(i).isEmpty()) {
            widgets.addSlot(EmiStack.of(ring.get(i)), RING[i][0], RING[i][1]);
         }
      }

      widgets.addTexture(EmiTexture.EMPTY_ARROW, 70, 19);
      widgets.addSlot(EmiStack.of(this.recipe.output()), 102, 18).recipeContext(this);

      String power = LocaleUtil.localize("gui.jei.category.buildcraft.assembly_table.power", LocaleUtil.localizeMj(this.recipe.microJoules()));
      widgets.addText(Component.literal(power), 0, 56, BcEmi.TEXT_COLOUR, false);
   }
}

package buildcraft.lib.client.guide.parts;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.entry.ItemStackValueFilter;
import buildcraft.lib.client.guide.entry.PageEntryItemStack;
import buildcraft.lib.client.guide.entry.PageValue;
import buildcraft.lib.client.guide.parts.recipe.RecipeLookupHelper;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.world.item.ItemStack;

@Deprecated
public class GuidePageStandInRecipes extends GuidePage {
   public GuidePageStandInRecipes(GuiGuide gui, List<GuidePart> parts, ItemStack stack) {
      super(gui, parts, new PageValue<>(PageEntryItemStack.INSTANCE, new ItemStackValueFilter(stack)));
   }

   @Nonnull
   public static GuidePageFactory createFactory(@Nonnull ItemStack stack) {
      return gui -> {
         List<GuidePart> parts = new ArrayList<>();
         List<GuidePartFactory> recipes = RecipeLookupHelper.getAllRecipes(stack);
         if (!recipes.isEmpty()) {
            parts.add(new GuideText(gui, "Recipes:"));

            for (GuidePartFactory factory : recipes) {
               GuidePart part = factory.createNew(gui);
               if (part != null) {
                  parts.add(part);
               }
            }
         }

         List<GuidePartFactory> usages = RecipeLookupHelper.getAllUsages(stack);
         if (!usages.isEmpty()) {
            parts.add(new GuideText(gui, "Usages:"));

            for (GuidePartFactory factory : usages) {
               GuidePart part = factory.createNew(gui);
               if (part != null) {
                  parts.add(part);
               }
            }
         }

         if (parts.isEmpty()) {
            parts.add(new GuideText(gui, "No recipes found."));
         }

         return new GuidePageStandInRecipes(gui, parts, stack);
      };
   }

   @Override
   public boolean shouldPersistHistory() {
      return false;
   }

   @Override
   public GuidePageBase createReloaded() {
      return this;
   }
}

package buildcraft.lib.misc;

import buildcraft.lib.tile.ItemHandlerSimple;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.level.Level;

public final class CraftingUtil {
   private CraftingUtil() {
   }

   @Nullable
   public static RecipeHolder<CraftingRecipe> findMatchingRecipe(CraftingInput input, Level level) {
      return level instanceof ServerLevel serverLevel
         ? (RecipeHolder)serverLevel.recipeAccess().getRecipeFor(RecipeType.CRAFTING, input, serverLevel).orElse(null)
         : null;
   }

   public static void placeRecipeInBlueprint(CraftingRecipe recipe, ItemHandlerSimple blueprint, Level level) {
      for (int i = 0; i < blueprint.getSlots(); i++) {
         blueprint.setStackInSlot(i, ItemStack.EMPTY);
      }

      ContextMap ctx = SlotDisplayContext.fromLevel(level);
      List<RecipeDisplay> displays = recipe.display();
      if (!displays.isEmpty()) {
         RecipeDisplay display = displays.get(0);
         if (display instanceof ShapelessCraftingRecipeDisplay shapeless) {
            for (int i = 0; i < shapeless.ingredients().size() && i < 9; i++) {
               ItemStack stack = firstStack((SlotDisplay)shapeless.ingredients().get(i), ctx);
               if (!stack.isEmpty()) {
                  blueprint.setStackInSlot(i, stack);
               }
            }
         } else if (display instanceof ShapedCraftingRecipeDisplay shaped) {
            int w = shaped.width();
            int h = shaped.height();

            for (int row = 0; row < h && row < 3; row++) {
               for (int col = 0; col < w && col < 3; col++) {
                  int idx = col + row * w;
                  if (idx < shaped.ingredients().size()) {
                     ItemStack stack = firstStack((SlotDisplay)shaped.ingredients().get(idx), ctx);
                     if (!stack.isEmpty()) {
                        blueprint.setStackInSlot(col + row * 3, stack);
                     }
                  }
               }
            }
         }
      }
   }

   private static ItemStack firstStack(SlotDisplay slotDisplay, ContextMap ctx) {
      List<ItemStack> stacks = slotDisplay.resolveForStacks(ctx);
      return stacks.isEmpty() ? ItemStack.EMPTY : stacks.get(0).copy();
   }
}

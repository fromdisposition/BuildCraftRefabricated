/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import buildcraft.lib.tile.ItemHandlerSimple;
import java.util.List;
import org.jspecify.annotations.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
//? if >= 1.21.10 {
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
//?}
import net.minecraft.world.level.Level;

public final class CraftingUtil {
   private CraftingUtil() {
   }

   @Nullable
   public static RecipeHolder<CraftingRecipe> findMatchingRecipe(CraftingInput input, Level level) {
      if (!(level instanceof ServerLevel serverLevel)) {
         return null;
      }
      //? if >= 1.21.10 {
      return serverLevel.recipeAccess().getRecipeFor(RecipeType.CRAFTING, input, serverLevel).orElse(null);
      //?} else {
      /*return serverLevel.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, serverLevel).orElse(null);
      *///?}
   }

   public static void placeRecipeInBlueprint(CraftingRecipe recipe, ItemHandlerSimple blueprint, Level level) {
      for (int i = 0; i < blueprint.getSlots(); i++) {
         blueprint.setStackInSlot(i, ItemStack.EMPTY);
      }

      //? if >= 1.21.10 {
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
      //?} else {
      /*if (recipe instanceof ShapedRecipe shaped) {
         int w = shaped.getWidth();
         int h = shaped.getHeight();
         NonNullList<Ingredient> ingredients = shaped.getIngredients();
         for (int row = 0; row < h && row < 3; row++) {
            for (int col = 0; col < w && col < 3; col++) {
               int idx = col + row * w;
               if (idx < ingredients.size()) {
                  ItemStack stack = ingredientToStack(ingredients.get(idx));
                  if (!stack.isEmpty()) {
                     blueprint.setStackInSlot(col + row * 3, stack);
                  }
               }
            }
         }
      } else {
         NonNullList<Ingredient> ingredients = recipe.getIngredients();
         for (int i = 0; i < ingredients.size() && i < 9; i++) {
            ItemStack stack = ingredientToStack(ingredients.get(i));
            if (!stack.isEmpty()) {
               blueprint.setStackInSlot(i, stack);
            }
         }
      }
      *///?}
   }

   //? if >= 1.21.10 {
   private static ItemStack firstStack(SlotDisplay slotDisplay, ContextMap ctx) {
      List<ItemStack> stacks = slotDisplay.resolveForStacks(ctx);
      return stacks.isEmpty() ? ItemStack.EMPTY : stacks.get(0).copy();
   }
   //?} else {
   /*private static ItemStack ingredientToStack(Ingredient ingredient) {
      for (ItemStack candidate : ingredient.getItems()) {
         if (!candidate.isEmpty() && candidate.getItem() != Items.AIR) {
            return candidate.copy();
         }
      }
      return ItemStack.EMPTY;
   }
   *///?}
}

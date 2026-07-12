/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts.recipe;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.parts.GuidePart;
import buildcraft.lib.client.guide.parts.GuidePartFactory;
import buildcraft.lib.misc.ItemStackKey;
import buildcraft.lib.recipe.ChangingItemStack;
import buildcraft.lib.recipe.IRecipeViewable;
import buildcraft.lib.recipe.ShapelessRecipes;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
//? if >= 1.21.10 {
import net.minecraft.util.context.ContextMap;
import net.minecraft.util.context.ContextMap.Builder;
//?}
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
//? if >= 1.21.10 {
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
//?}

public class GuideCraftingFactory implements GuidePartFactory {
   private final ChangingItemStack[][] input;
   private final ChangingItemStack output;
   private final int hash;

   public GuideCraftingFactory(ChangingItemStack[][] input, ChangingItemStack output) {
      this.input = input;
      this.output = output;
      int h = 0;

      for (ChangingItemStack[] row : input) {
         for (ChangingItemStack stack : row) {
            h = h * 31 + stack.hashCode();
         }
      }

      this.hash = h * 31 + output.hashCode();
   }

   public boolean outputMatches(ItemStack target) {
      return this.output.matches(target);
   }

   @Nullable
   public static GuidePartFactory getFactory(CraftingRecipe recipe) {
      if (recipe instanceof IRecipeViewable) {
         return getFactoryFromViewable((IRecipeViewable)recipe);
      } else if (recipe instanceof ShapedRecipe shaped) {
         return getFactoryFromShaped(shaped);
      } else {
         return recipe instanceof ShapelessRecipe shapeless ? getFactoryFromShapeless(shapeless) : null;
      }
   }

   @Nullable
   public static GuidePartFactory getCyclingFactory(List<CraftingRecipe> recipes) {
      List<GuideCraftingFactory> singles = new ArrayList<>();

      for (CraftingRecipe recipe : recipes) {
         if (getFactory(recipe) instanceof GuideCraftingFactory gcf) {
            singles.add(gcf);
         }
      }

      if (singles.isEmpty()) {
         return null;
      }

      if (singles.size() == 1) {
         return singles.get(0);
      }

      ChangingItemStack[][] input = new ChangingItemStack[3][3];

      for (int x = 0; x < 3; x++) {
         for (int y = 0; y < 3; y++) {
            List<ItemStack> options = new ArrayList<>(singles.size());

            for (GuideCraftingFactory single : singles) {
               options.add(firstOption(single.input[x][y]));
            }

            input[x][y] = new ChangingItemStack(options);
         }
      }

      List<ItemStack> outputs = new ArrayList<>(singles.size());

      for (GuideCraftingFactory single : singles) {
         outputs.add(firstOption(single.output));
      }

      return new GuideCraftingFactory(input, new ChangingItemStack(outputs));
   }

   private static ItemStack firstOption(ChangingItemStack stack) {
      List<ItemStackKey> options = stack.getOptions();
      return options.isEmpty() ? ItemStack.EMPTY : options.get(0).baseStack;
   }

   private static ItemStack assembleCrafting(net.minecraft.world.item.crafting.Recipe<CraftingInput> recipe, CraftingInput input) {
      //? if >= 26.1 {
      return recipe.assemble(input);
      //?} else {
      /*return recipe.assemble(input, net.minecraft.client.Minecraft.getInstance().level.registryAccess());
      *///?}
   }

   private static GuidePartFactory getFactoryFromShaped(ShapedRecipe recipe) {
      ItemStack output = assembleCrafting(recipe, CraftingInput.EMPTY);
      if (output.isEmpty()) {
         return null;
      }

      int width = recipe.getWidth();
      int height = recipe.getHeight();
      //? if >= 1.21.10 {
      List<Optional<Ingredient>> ingredients = recipe.getIngredients();
      //?} else {
      /*net.minecraft.core.NonNullList<Ingredient> ingredients = recipe.getIngredients();
      *///?}
      int offsetX = width == 1 ? 1 : 0;
      int offsetY = height == 1 ? 1 : 0;
      ChangingItemStack[][] matrix = new ChangingItemStack[3][3];

      for (int y = 0; y < 3; y++) {
         for (int x = 0; x < 3; x++) {
            if (x >= offsetX && y >= offsetY) {
               int i = x - offsetX + (y - offsetY) * width;
               if (i < ingredients.size() && x - offsetX < width) {
                  //? if >= 1.21.10 {
                  Optional<Ingredient> opt = ingredients.get(i);
                  matrix[x][y] = opt.map(GuideCraftingFactory::ingredientToChanging).orElse(new ChangingItemStack(ItemStack.EMPTY));
                  //?} else {
                  /*Ingredient ing = ingredients.get(i);
                  matrix[x][y] = ing == null ? new ChangingItemStack(ItemStack.EMPTY) : ingredientToChanging(ing);
                  *///?}
               } else {
                  matrix[x][y] = new ChangingItemStack(ItemStack.EMPTY);
               }
            } else {
               matrix[x][y] = new ChangingItemStack(ItemStack.EMPTY);
            }
         }
      }

      return new GuideCraftingFactory(matrix, new ChangingItemStack(output));
   }

   private static GuidePartFactory getFactoryFromShapeless(ShapelessRecipe recipe) {
      ItemStack output = assembleCrafting(recipe, CraftingInput.EMPTY);
      List<Ingredient> ingredients = new ArrayList<>();

      for (Ingredient ingredient : ShapelessRecipes.ingredients(recipe)) {
         ingredients.add(ingredient);
      }

      if (!ingredients.isEmpty() && !output.isEmpty()) {
         ChangingItemStack[][] matrix = new ChangingItemStack[3][3];

         for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
               int i = x + y * 3;
               if (i < ingredients.size()) {
                  matrix[x][y] = ingredientToChanging(ingredients.get(i));
               } else {
                  matrix[x][y] = new ChangingItemStack(ItemStack.EMPTY);
               }
            }
         }

         return new GuideCraftingFactory(matrix, new ChangingItemStack(output));
      } else {
         return null;
      }
   }

   @Nullable
   private static GuidePartFactory getFactoryFromViewable(IRecipeViewable viewable) {
      ChangingItemStack[] inputs = viewable.getRecipeInputs();
      ChangingItemStack outputs = viewable.getRecipeOutputs();
      if (inputs != null && outputs != null) {
         int width = 3;
         int height = 3;
         if (viewable instanceof IRecipeViewable.IViewableGrid grid) {
            width = grid.getRecipeWidth();
            height = grid.getRecipeHeight();
         }

         ChangingItemStack[][] matrix = new ChangingItemStack[3][3];
         int offsetX = width == 1 ? 1 : 0;
         int offsetY = height == 1 ? 1 : 0;

         for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
               if (x >= offsetX && y >= offsetY) {
                  int i = x - offsetX + (y - offsetY) * width;
                  if (i < inputs.length && x - offsetX < width) {
                     matrix[x][y] = inputs[i];
                  } else {
                     matrix[x][y] = new ChangingItemStack(ItemStack.EMPTY);
                  }
               } else {
                  matrix[x][y] = new ChangingItemStack(ItemStack.EMPTY);
               }
            }
         }

         return new GuideCraftingFactory(matrix, outputs);
      } else {
         return null;
      }
   }

   //? if >= 1.21.10 {
   static ContextMap displayContext() {
      Minecraft mc = Minecraft.getInstance();
      ClientLevel level = mc == null ? null : mc.level;
      return level != null ? SlotDisplayContext.fromLevel(level) : new Builder().create(SlotDisplayContext.CONTEXT);
   }
   //?}

   static ChangingItemStack ingredientToChanging(Ingredient ingredient) {
      List<ItemStack> stacks = new ArrayList<>();

      //? if >= 1.21.10 {
      ContextMap ctx = displayContext();
      for (ItemStack stack : ingredient.display().resolveForStacks(ctx)) {
      //?} else {
      /*// 1.21.1: Ingredient exposes the resolved stacks directly, no SlotDisplay/ContextMap.
      for (ItemStack stack : ingredient.getItems()) {
      *///?}
         if (!stack.isEmpty() && stack.getItem() != Items.AIR) {
            stacks.add(stack);
         }
      }

      return stacks.isEmpty() ? new ChangingItemStack(ItemStack.EMPTY) : new ChangingItemStack(stacks);
   }

   @Override
   public GuidePart createNew(GuiGuide gui) {
      return new GuideCrafting(gui, this.input, this.output);
   }

   @Override
   public int hashCode() {
      return this.hash;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      }

      if (obj == null) {
         return false;
      }

      if (obj.getClass() != this.getClass()) {
         return false;
      }

      GuideCraftingFactory other = (GuideCraftingFactory)obj;
      if (this.hash != other.hash) {
         return false;
      }

      if (this.input.length != other.input.length) {
         return false;
      }

      for (int x = 0; x < this.input.length; x++) {
         if (this.input[x].length != other.input[x].length) {
            return false;
         }

         for (int y = 0; y < this.input[x].length; y++) {
            if (!this.input[x][y].equals(other.input[x][y])) {
               return false;
            }
         }
      }

      return this.output.equals(other.output);
   }
}

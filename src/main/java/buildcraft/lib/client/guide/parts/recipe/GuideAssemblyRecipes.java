/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts.recipe;

import buildcraft.api.recipes.AssemblyRecipe;
import buildcraft.api.recipes.IngredientStack;
import buildcraft.lib.client.guide.parts.GuidePartFactory;
import buildcraft.lib.recipe.AssemblyRecipeRegistry;
import buildcraft.lib.recipe.ChangingItemStack;
import buildcraft.lib.recipe.ChangingObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public enum GuideAssemblyRecipes implements IStackRecipes {
   INSTANCE;

   @Override
   public List<GuidePartFactory> getUsages(@Nonnull ItemStack stack) {
      List<GuidePartFactory> list = new ArrayList<>();

      for (AssemblyRecipe recipe : AssemblyRecipeRegistry.REGISTRY.values()) {
         for (ItemStack out : recipe.getOutputPreviews()) {
            boolean isUsed = false;

            for (IngredientStack ing : recipe.getInputsFor(out)) {
               if (ing.ingredient.test(stack)) {
                  isUsed = true;
                  break;
               }
            }

            if (isUsed) {
               list.add(createFactory(recipe, out));
            }
         }
      }

      return list;
   }

   @Override
   public List<GuidePartFactory> getRecipes(@Nonnull ItemStack stack) {
      List<GuidePartFactory> list = new ArrayList<>();

      for (AssemblyRecipe recipe : AssemblyRecipeRegistry.REGISTRY.values()) {
         for (ItemStack out : recipe.getOutputPreviews()) {
            if (ItemStack.isSameItem(stack, out)) {
               list.add(createFactory(recipe, out));
            }
         }
      }

      return list;
   }

   @Nullable
   public static GuidePartFactory getFactoryByName(String name) {
      AssemblyRecipe recipe = AssemblyRecipeRegistry.REGISTRY.get(name);
      if (recipe == null) {
         return null;
      } else {
         Iterator var2 = recipe.getOutputPreviews().iterator();
         if (var2.hasNext()) {
            ItemStack out = (ItemStack)var2.next();
            return createFactory(recipe, out);
         } else {
            return null;
         }
      }
   }

   private static GuidePartFactory createFactory(AssemblyRecipe recipe, ItemStack output) {
      return new GuideAssemblyFactory(resolveInputStacks(recipe, output), output, recipe.getRequiredMicroJoulesFor(output));
   }

   private static ItemStack[] resolveInputStacks(AssemblyRecipe recipe, ItemStack output) {
      Set<IngredientStack> inputs = recipe.getInputsFor(output);
      ContextMap ctx = GuideCraftingFactory.displayContext();
      ItemStack[] inStacks = new ItemStack[inputs.size()];
      int i = 0;

      for (IngredientStack ing : inputs) {
         List<ItemStack> resolved = ing.ingredient.display().resolveForStacks(ctx);
         ItemStack first = ItemStack.EMPTY;

         for (ItemStack candidate : resolved) {
            if (!candidate.isEmpty() && candidate.getItem() != Items.AIR) {
               first = candidate;
               break;
            }
         }

         if (!first.isEmpty()) {
            ItemStack rep = first.copy();
            rep.setCount(ing.count);
            inStacks[i++] = rep;
         } else {
            inStacks[i++] = ItemStack.EMPTY;
         }
      }

      return inStacks;
   }

   public static List<AssemblyRecipe> gatherByNameMatch(String substring) {
      List<AssemblyRecipe> matched = new ArrayList<>();

      for (Entry<String, AssemblyRecipe> entry : new TreeMap<>(AssemblyRecipeRegistry.REGISTRY).entrySet()) {
         if (entry.getKey().contains(substring)) {
            matched.add(entry.getValue());
         }
      }

      return matched;
   }

   @Nullable
   public static GuidePartFactory getCyclingFactoryByNameMatch(String substring) {
      return getCyclingFactory(gatherByNameMatch(substring));
   }

   @Nullable
   public static GuidePartFactory getCyclingFactory(List<AssemblyRecipe> recipes) {
      List<ItemStack[]> inputsPerRecipe = new ArrayList<>();
      List<ItemStack> outputs = new ArrayList<>();
      List<Long> mjCosts = new ArrayList<>();
      int maxSlots = 0;

      for (AssemblyRecipe recipe : recipes) {
         ItemStack output = ItemStack.EMPTY;
         Iterator inStacks = recipe.getOutputPreviews().iterator();
         if (inStacks.hasNext()) {
            ItemStack out = (ItemStack)inStacks.next();
            output = out;
         }

         if (!output.isEmpty()) {
            ItemStack[] inStacksx = resolveInputStacks(recipe, output);
            inputsPerRecipe.add(inStacksx);
            outputs.add(output);
            mjCosts.add(recipe.getRequiredMicroJoulesFor(output));
            maxSlots = Math.max(maxSlots, inStacksx.length);
         }
      }

      if (outputs.isEmpty()) {
         return null;
      }

      if (outputs.size() == 1) {
         return new GuideAssemblyFactory(inputsPerRecipe.get(0), outputs.get(0), mjCosts.get(0));
      }

      ChangingItemStack[] input = new ChangingItemStack[maxSlots];

      for (int slot = 0; slot < maxSlots; slot++) {
         List<ItemStack> options = new ArrayList<>(outputs.size());

         for (ItemStack[] in : inputsPerRecipe) {
            options.add(slot < in.length ? in[slot] : ItemStack.EMPTY);
         }

         input[slot] = new ChangingItemStack(options);
      }

      ChangingItemStack output = new ChangingItemStack(outputs);
      ChangingObject<Long> mjCost = new ChangingObject<>(mjCosts.toArray(new Long[0]));
      return new GuideAssemblyFactory(input, output, mjCost);
   }
}

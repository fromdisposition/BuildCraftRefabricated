/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.recipe;

import buildcraft.api.recipes.IProgrammingRecipe;
import buildcraft.api.recipes.IProgrammingRecipeRegistry;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.world.item.ItemStack;

public class ProgrammingRecipeRegistry implements IProgrammingRecipeRegistry {
   public static final ProgrammingRecipeRegistry INSTANCE = new ProgrammingRecipeRegistry();
   private final Map<String, IProgrammingRecipe> recipes = new HashMap<>();

   @Override
   public void addRecipe(IProgrammingRecipe recipe) {
      this.recipes.putIfAbsent(recipe.getId(), recipe);
   }

   @Override
   public Iterable<IProgrammingRecipe> getRecipes() {
      return this.recipes.values();
   }

   @Override
   @Nullable
   public IProgrammingRecipe getRecipe(String id) {
      return id == null || id.isEmpty() ? null : this.recipes.get(id);
   }

   @Override
   @Nullable
   public IProgrammingRecipe getRecipeFor(ItemStack input) {
      if (input.isEmpty()) {
         return null;
      }

      for (IProgrammingRecipe recipe : this.recipes.values()) {
         if (recipe.canCraft(input)) {
            return recipe;
         }
      }

      return null;
   }
}

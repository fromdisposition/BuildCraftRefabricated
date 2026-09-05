/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.recipe;

import buildcraft.api.recipes.AssemblyRecipe;
import java.util.HashMap;
import java.util.Map;

public class AssemblyRecipeRegistry {
   public static final Map<String, AssemblyRecipe> REGISTRY = new HashMap<>();

   public static void register(AssemblyRecipe recipe) {
      REGISTRY.put(recipe.getRegistryName(), recipe);
   }
}

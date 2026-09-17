/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.recipe;

import buildcraft.lib.fabric.mixin.ShapelessRecipeAccessor;
import java.util.List;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;

public final class ShapelessRecipes {
   private ShapelessRecipes() {
   }

   public static List<Ingredient> ingredients(ShapelessRecipe recipe) {
      return ((ShapelessRecipeAccessor)recipe).buildcraft$getIngredients();
   }
}

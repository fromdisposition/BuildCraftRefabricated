/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.recipe;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;

import buildcraft.api.recipes.IIntegrationRecipeRegistry;
import buildcraft.api.recipes.IntegrationRecipe;

public class IntegrationRecipeRegistry implements IIntegrationRecipeRegistry {
    public static final IntegrationRecipeRegistry INSTANCE = new IntegrationRecipeRegistry();
    public final Map<Object, IntegrationRecipe> recipes = new HashMap<>();

    @Override
    public IntegrationRecipe getRecipeFor(@Nonnull ItemStack target, @Nonnull NonNullList<ItemStack> toIntegrate) {
        for (IntegrationRecipe recipe : recipes.values()) {
            if (!recipe.getOutput(target, toIntegrate).isEmpty()) {
                return recipe;
            }
        }
        return null;
    }

    @Override
    public void addRecipe(IntegrationRecipe recipe) {
        if (recipes.putIfAbsent(recipe.name, recipe) != null) {
            return;
        }
    }

    @Override
    public Iterable<IntegrationRecipe> getAllRecipes() {
        return recipes.values();
    }

    @Override
    public IntegrationRecipe getRecipe(@Nonnull Object name) {
        return recipes.get(name);
    }
}

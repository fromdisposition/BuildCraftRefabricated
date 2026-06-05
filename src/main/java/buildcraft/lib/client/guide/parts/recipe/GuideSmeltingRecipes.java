/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts.recipe;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.block.Blocks;

import buildcraft.lib.client.guide.parts.GuidePartFactory;

public enum GuideSmeltingRecipes implements IStackRecipes {
    INSTANCE;

    private static final SingleRecipeInput EMPTY_INPUT = new SingleRecipeInput(ItemStack.EMPTY);

    @Override
    public List<GuidePartFactory> getUsages(@Nonnull ItemStack stack) {
        RecipeManager manager = GuideCraftingRecipes.getRecipeManager();
        if (manager == null) return ImmutableList.of();

        if (stack.is(Blocks.FURNACE.asItem())) {
            List<GuidePartFactory> list = new ArrayList<>();
            for (RecipeHolder<?> holder : manager.getRecipes()) {
                if (holder.value() instanceof SmeltingRecipe smelt) {
                    ItemStack output = smelt.assemble(EMPTY_INPUT);
                    if (!output.isEmpty()) {
                        list.add(new GuideSmeltingFactory(getIngredientStack(smelt), output));
                    }
                }
            }
            return list;
        }

        List<GuidePartFactory> list = new ArrayList<>();
        for (RecipeHolder<?> holder : manager.getRecipes()) {
            if (holder.value() instanceof SmeltingRecipe smelt) {
                if (smelt.input().test(stack)) {
                    ItemStack output = smelt.assemble(EMPTY_INPUT);
                    if (!output.isEmpty()) {
                        list.add(new GuideSmeltingFactory(stack, output));
                    }
                }
            }
        }
        return list.isEmpty() ? null : list;
    }

    @Override
    public List<GuidePartFactory> getRecipes(@Nonnull ItemStack stack) {
        RecipeManager manager = GuideCraftingRecipes.getRecipeManager();
        if (manager == null) return ImmutableList.of();

        List<GuidePartFactory> list = new ArrayList<>();
        for (RecipeHolder<?> holder : manager.getRecipes()) {
            if (holder.value() instanceof SmeltingRecipe smelt) {
                ItemStack output = smelt.assemble(EMPTY_INPUT);
                if (!output.isEmpty() && ItemStack.isSameItem(stack, output)) {
                    list.add(new GuideSmeltingFactory(getIngredientStack(smelt), output));
                }
            }
        }
        return list;
    }

    private static ItemStack getIngredientStack(AbstractCookingRecipe recipe) {
        return GuideCraftingFactory.ingredientToChanging(recipe.input())
            .get().baseStack;
    }
}

/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;

import buildcraft.lib.client.guide.parts.GuidePartFactory;
import buildcraft.lib.misc.RegistryKeyUtil;

public enum GuideCraftingRecipes implements IStackRecipes {
    INSTANCE;

    @Override
    public List<GuidePartFactory> getUsages(@Nonnull ItemStack target) {
        List<GuidePartFactory> list = new ArrayList<>();
        for (RecipeHolder<?> holder : getAllRecipes()) {
            if (holder.value() instanceof CraftingRecipe recipe) {
                if (checkRecipeUses(recipe, target)) {
                    GuidePartFactory factory = GuideCraftingFactory.getFactory(recipe);
                    if (factory != null) {
                        list.add(factory);
                    }
                }
            }
        }
        return list;
    }

    @Override
    public List<GuidePartFactory> getRecipes(@Nonnull ItemStack target) {
        List<GuidePartFactory> list = new ArrayList<>();
        for (RecipeHolder<?> holder : getAllRecipes()) {
            if (holder.value() instanceof CraftingRecipe recipe) {

                GuidePartFactory factory = GuideCraftingFactory.getFactory(recipe);
                if (factory instanceof GuideCraftingFactory gcf && gcf.outputMatches(target)) {
                    list.add(factory);
                }
            }
        }
        return list;
    }

    public static List<CraftingRecipe> gatherByIdMatch(String substring) {
        RecipeManager manager = getRecipeManager();
        if (manager == null) {
            return new ArrayList<>();
        }
        java.util.TreeMap<String, CraftingRecipe> matched = new java.util.TreeMap<>();
        for (RecipeHolder<?> holder : manager.getRecipes()) {
            if (holder.value() instanceof CraftingRecipe crafting
                && RegistryKeyUtil.id(holder.id()).toString().contains(substring)) {
                matched.put(RegistryKeyUtil.id(holder.id()).toString(), crafting);
            }
        }
        return new ArrayList<>(matched.values());
    }

    @Nullable
    public static GuidePartFactory getCyclingFactoryByIdMatch(String substring) {
        return GuideCraftingFactory.getCyclingFactory(gatherByIdMatch(substring));
    }

    public void generateIndices() {

    }

    private static boolean checkRecipeUses(CraftingRecipe recipe, @Nonnull ItemStack target) {
        if (recipe instanceof ShapedRecipe shaped) {
            for (Optional<Ingredient> opt : shaped.getIngredients()) {
                if (opt.isPresent() && opt.get().test(target)) {
                    return true;
                }
            }
        } else if (recipe instanceof ShapelessRecipe shapeless) {
            for (Ingredient ingredient : buildcraft.lib.recipe.ShapelessRecipes.ingredients(shapeless)) {
                if (ingredient.test(target)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static Iterable<RecipeHolder<?>> getAllRecipes() {
        RecipeManager manager = getRecipeManager();
        if (manager == null) return ImmutableList.of();
        return manager.getRecipes();
    }

    @Nullable
    public static RecipeManager getRecipeManager() {
        Minecraft mc = Minecraft.getInstance();
        net.minecraft.server.MinecraftServer server = mc.getSingleplayerServer();
        if (server != null) {
            return server.getRecipeManager();
        }
        return null;
    }
}

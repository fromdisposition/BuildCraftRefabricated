/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.integration.jei;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import buildcraft.lib.fluids.FluidStack;

import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.RecipeIngredientRole;

import buildcraft.core.BCCoreItems;
import buildcraft.core.item.ItemFragileFluidContainer;

public final class FluidContainerAliases {

    private static final List<Provider> providers = new ArrayList<>();

    static {

        registerProvider((fluidStack, sink) -> {
            Item bucket = fluidStack.getFluid().getBucket();
            if (bucket != null && bucket != Items.AIR) {
                sink.accept(new ItemStack(bucket));
            }
        });

        registerProvider((fluidStack, sink) -> {
            if (BCCoreItems.FRAGILE_FLUID_CONTAINER == null) {
                return;
            }
            Item shardItem = BCCoreItems.FRAGILE_FLUID_CONTAINER;
            if (shardItem == null) {
                return;
            }
            ItemStack shard = new ItemStack(shardItem);
            FluidStack copy = fluidStack.copy();
            copy.setAmount(ItemFragileFluidContainer.MAX_FLUID_HELD);
            ItemFragileFluidContainer.setFluid(shard, copy);
            sink.accept(shard);
        });
    }

    private FluidContainerAliases() {
    }

    public static void registerProvider(Provider provider) {
        providers.add(provider);
    }

    public static void addAliases(IRecipeLayoutBuilder builder, FluidStack stack, RecipeIngredientRole role) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        IIngredientAcceptor<?> slot = builder.addInvisibleIngredients(role);
        for (Provider provider : providers) {
            provider.addAliases(stack, alias -> {
                if (alias != null && !alias.isEmpty()) {
                    slot.add(alias);
                }
            });
        }
    }

    @FunctionalInterface
    public interface Provider {
        void addAliases(FluidStack fluidStack, Consumer<ItemStack> sink);
    }
}


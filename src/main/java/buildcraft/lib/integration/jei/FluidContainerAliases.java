/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.integration.jei;

import buildcraft.core.BCCoreItems;
import buildcraft.core.item.ItemFragileFluidContainer;
import buildcraft.lib.fluid.stack.FluidStack;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class FluidContainerAliases {
   private static final List<FluidContainerAliases.Provider> providers = new ArrayList<>();

   private FluidContainerAliases() {
   }

   public static void registerProvider(FluidContainerAliases.Provider provider) {
      providers.add(provider);
   }

   public static void addAliases(IRecipeLayoutBuilder builder, FluidStack stack, RecipeIngredientRole role) {
      if (stack != null && !stack.isEmpty()) {
         IIngredientAcceptor<?> slot = builder.addInvisibleIngredients(role);

         for (FluidContainerAliases.Provider provider : providers) {
            provider.addAliases(stack, alias -> {
               if (alias != null && !alias.isEmpty()) {
                  //? if >= 1.21.10 {
                  slot.add(alias);
                  //?} else {
                  /*slot.addItemStack(alias);
                  *///?}
               }
            });
         }
      }
   }

   static {
      registerProvider((fluidStack, sink) -> {
         Item bucket = fluidStack.getFluid().getBucket();
         if (bucket != null && bucket != Items.AIR) {
            sink.accept(new ItemStack(bucket));
         }
      });
      registerProvider((fluidStack, sink) -> {
         if (BCCoreItems.FRAGILE_FLUID_CONTAINER != null) {
            Item shardItem = BCCoreItems.FRAGILE_FLUID_CONTAINER;
            if (shardItem != null) {
               ItemStack shard = new ItemStack(shardItem);
               FluidStack copy = fluidStack.copy();
               copy.setAmount(500);
               ItemFragileFluidContainer.setFluid(shard, copy);
               sink.accept(shard);
            }
         }
      });
   }

   @FunctionalInterface
   public interface Provider {
      void addAliases(FluidStack var1, Consumer<ItemStack> var2);
   }
}

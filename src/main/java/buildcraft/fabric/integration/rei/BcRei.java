/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.fabric.integration.rei;

import buildcraft.lib.integration.jei.FluidContainerAliases;
import buildcraft.lib.misc.LocaleUtil;
import dev.architectury.fluid.FluidStack;
import java.util.List;
import me.shedaniel.rei.api.client.util.ClientEntryStacks;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

final class BcRei {
   static final int MB_TO_DROPLETS = 81;

   private BcRei() {
   }

   static EntryIngredient fluid(Fluid fluid, int milliBuckets) {
      if (fluid == null || milliBuckets <= 0) {
         return EntryIngredient.empty();
      }

      long droplets = (long)milliBuckets * MB_TO_DROPLETS;
      EntryStack<FluidStack> stack = EntryStacks.of(FluidStack.create(fluid, droplets));
      ClientEntryStacks.setFluidRenderRatio(stack, 0.0F);
      String dropletLine = LocaleUtil.localize("tooltip.rei.fluid_amount", droplets);
      Component mbLine = Component.literal(milliBuckets + " mB");
      stack.tooltipProcessor((s, tooltip) -> {
         tooltip.entries().removeIf(entry -> entry.isText() && dropletLine.equals(entry.getAsText().getString()));
         tooltip.add(mbLine);
         return tooltip;
      });
      return EntryIngredient.of(stack);
   }

   static EntryIngredient fluid(buildcraft.lib.fluid.stack.FluidStack stack) {
      return stack == null || stack.isEmpty() ? EntryIngredient.empty() : fluid(stack.getFluid(), stack.getAmount());
   }

   static EntryIngredient item(ItemStack stack) {
      return stack == null || stack.isEmpty() ? EntryIngredient.empty() : EntryIngredients.of(stack);
   }

   static List<EntryIngredient> withFluidAliases(List<EntryIngredient> entries) {
      List<ItemStack> aliases = new java.util.ArrayList<>(0);
      for (EntryIngredient ingredient : entries) {
         for (EntryStack<?> entry : ingredient) {
            if (entry.getValue() instanceof FluidStack fluid && !fluid.isEmpty()) {
               var bcStack = new buildcraft.lib.fluid.stack.FluidStack(fluid.getFluid(), (int)(fluid.getAmount() / MB_TO_DROPLETS));
               FluidContainerAliases.collectAliases(bcStack, aliases::add);
            }
         }
      }

      if (aliases.isEmpty()) {
         return entries;
      }

      List<EntryIngredient> withAliases = new java.util.ArrayList<>(entries);
      withAliases.add(EntryIngredients.ofItemStacks(aliases));
      return withAliases;
   }

   static EntryIngredient itemAlternatives(List<ItemStack> stacks) {
      return stacks.isEmpty() ? EntryIngredient.empty() : EntryIngredients.ofItemStacks(stacks);
   }

   static Component mjText(String key, long microMj) {
      return Component.literal(LocaleUtil.localize(key, LocaleUtil.localizeMj(microMj)));
   }
}

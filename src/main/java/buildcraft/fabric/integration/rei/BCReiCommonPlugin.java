/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.fabric.integration.rei;

import buildcraft.core.BCCore;
import buildcraft.core.BCCoreItems;
import buildcraft.core.item.ItemFragileFluidContainer;
import buildcraft.lib.fluid.stack.FluidStack;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.silicon.BCSiliconItems;
import buildcraft.silicon.item.ItemPluggableGate;
import buildcraft.silicon.item.ItemPluggableLens;
import java.util.Objects;
import me.shedaniel.rei.api.common.entry.comparison.EntryComparator;
import me.shedaniel.rei.api.common.entry.comparison.ItemComparatorRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

/** Mirrors the JEI subtype interpreters, so REI lists and matches these items per variant. */
//? if >= 1.21.10 {
public final class BCReiCommonPlugin implements me.shedaniel.rei.api.common.plugins.REICommonPlugin {
//?} else {
/*public final class BCReiCommonPlugin implements me.shedaniel.rei.api.common.plugins.REIServerPlugin {
*///?}
   @Override
   public void registerItemComparators(ItemComparatorRegistry registry) {
      registry.register(by(stack -> stack.get(BCCore.BRUSH_COLOR)), BCCoreItems.PAINTBRUSH);
      registry.register(by(stack -> {
         FluidStack fluid = ItemFragileFluidContainer.getFluid(stack);
         return fluid.isEmpty() ? null : BuiltInRegistries.FLUID.getKey(fluid.getFluid());
      }), BCCoreItems.FRAGILE_FLUID_CONTAINER);
      registry.register(by(stack -> {
         DyeColor colour = ItemPluggableLens.getColour(stack);
         return (colour == null ? "clear" : colour.getName()) + ":" + ItemPluggableLens.isFilter(stack);
      }), BCSiliconItems.PLUG_LENS);
      registry.register(by(stack -> ItemPluggableGate.getVariant(stack).getVariantName()), BCSiliconItems.PLUG_GATE);
      //? if >= 1.21.10 {
      registry.register(by(stack -> NBTUtilBC.getItemData(stack).getCompoundOrEmpty("facade")), BCSiliconItems.PLUG_FACADE);
      //?} else {
      /*registry.register(by(stack -> NBTUtilBC.getItemData(stack).getCompound("facade")), BCSiliconItems.PLUG_FACADE);
      *///?}
   }

   private static EntryComparator<ItemStack> by(java.util.function.Function<ItemStack, Object> key) {
      return (context, stack) -> Objects.hashCode(key.apply(stack));
   }
}

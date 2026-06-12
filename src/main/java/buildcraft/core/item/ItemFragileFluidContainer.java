/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.item;

import buildcraft.api.items.IItemFluidShard;
import buildcraft.core.BCCore;
import buildcraft.lib.fluid.stack.FluidStack;
import buildcraft.lib.fluid.stack.SimpleFluidContent;
import buildcraft.lib.fluid.BcFluids;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
public class ItemFragileFluidContainer extends Item implements IItemFluidShard {
   public static final int MAX_FLUID_HELD = 500;

   public ItemFragileFluidContainer(Properties properties) {
      super(properties.stacksTo(1));
   }

   public Component getName(ItemStack stack) {
      FluidStack fluid = getFluid(stack);
      return fluid.isEmpty()
         ? Component.translatable(this.getDescriptionId() + ".name.empty")
         : Component.translatable(this.getDescriptionId() + ".name", BcFluids.getFluidDisplayName(fluid));
   }

   public static void appendTooltipLines(ItemFragileFluidContainer item, ItemStack stack, TooltipFlag flag, List<Component> tooltip) {
      FluidStack fluid = getFluid(stack);
      if (!fluid.isEmpty() && fluid.getAmount() > 0) {
         tooltip.add(Component.literal(fluid.getAmount() + " mB / 500 mB"));
      }
   }

   @Override
   public void addFluidDrops(NonNullList<ItemStack> toDrop, @Nullable FluidStack fluid) {
      if (fluid != null && !fluid.isEmpty()) {
         int amount = fluid.getAmount();
         if (amount >= 500) {
            FluidStack fluid2 = fluid.copy();
            fluid2.setAmount(500);

            while (amount >= 500) {
               ItemStack stack = new ItemStack(this);
               setFluid(stack, fluid2);
               amount -= 500;
               toDrop.add(stack);
            }
         }

         if (amount > 0) {
            ItemStack stack = new ItemStack(this);
            FluidStack fluid2 = fluid.copy();
            fluid2.setAmount(amount);
            setFluid(stack, fluid2);
            toDrop.add(stack);
         }
      }
   }

   public static void setFluid(ItemStack container, FluidStack fluid) {
      if (fluid.isEmpty()) {
         container.remove(BCCore.FLUID_CONTENT);
      } else {
         container.set(BCCore.FLUID_CONTENT, SimpleFluidContent.copyOf(fluid));
      }
   }

   public static FluidStack getFluid(ItemStack container) {
      if (container.isEmpty()) {
         return FluidStack.EMPTY;
      }

      SimpleFluidContent content = (SimpleFluidContent)container.getOrDefault(BCCore.FLUID_CONTENT, SimpleFluidContent.EMPTY);
      return content.copy();
   }
}

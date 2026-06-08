/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.core;

import buildcraft.lib.fluids.FluidStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public final class StackKey {
   public final ItemStack stack;
   public final FluidStack fluidStack;

   private StackKey(ItemStack stack, FluidStack fluidStack) {
      this.stack = stack;
      this.fluidStack = fluidStack;
   }

   private StackKey(ItemStack stack) {
      this(stack, null);
   }

   private StackKey(FluidStack stack) {
      this(null, stack);
   }

   public static StackKey stack(Item item, int amount, int damage) {
      return new StackKey(new ItemStack(item, amount));
   }

   public static StackKey stack(Block block, int amount, int damage) {
      return new StackKey(new ItemStack(block, amount));
   }

   public static StackKey stack(Item item) {
      return new StackKey(new ItemStack(item, 1));
   }

   public static StackKey stack(Block block) {
      return new StackKey(new ItemStack(block, 1));
   }

   public static StackKey fluid(FluidStack fluidStack) {
      return new StackKey(fluidStack);
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof StackKey k)) {
         return false;
      } else if (this.stack == null != (k.stack == null) || this.fluidStack == null != (k.fluidStack == null)) {
         return false;
      } else {
         return this.stack != null && !ItemStack.isSameItem(this.stack, k.stack)
            ? false
            : this.fluidStack == null || FluidStack.isSameFluidSameComponents(this.fluidStack, k.fluidStack);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      if (this.stack != null) {
         result = 31 * result + this.stack.getItem().hashCode();
      }

      if (this.fluidStack != null) {
         result = 31 * result + this.fluidStack.getFluid().hashCode();
         result = 31 * result + this.fluidStack.getAmount();
      }

      return result;
   }

   @Override
   public String toString() {
      if (this.stack != null) {
         return "StackKey{item=" + this.stack + "}";
      } else {
         return this.fluidStack != null ? "StackKey{fluid=" + this.fluidStack + "}" : "StackKey{empty}";
      }
   }
}

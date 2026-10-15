/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.inventory;

import buildcraft.api.core.IStackFilter;
import buildcraft.api.inventory.IItemTransactor;
import buildcraft.lib.inventory.filter.StackFilter;
import buildcraft.lib.misc.StackUtil;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.Arrays;
import javax.annotation.Nonnull;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

public abstract class AbstractInvItemTransactor implements IItemTransactor {
   @Nonnull
   public static ItemStack asValid(@Nonnull ItemStack stack) {
      return stack.isEmpty() ? StackUtil.EMPTY : stack;
   }

   @Nonnull
   protected abstract ItemStack insert(int var1, @Nonnull ItemStack var2, boolean var3);

   @Nonnull
   protected abstract ItemStack extract(int var1, IStackFilter var2, int var3, int var4, boolean var5);

   protected abstract int size();

   protected abstract boolean isEmpty(int var1);

   @Nonnull
   @Override
   public ItemStack insert(@Nonnull ItemStack stack, boolean allAtOnce, boolean simulate) {
      return allAtOnce ? this.insertAllAtOnce(stack, simulate) : this.insertAnyAmount(stack, simulate);
   }

   @Nonnull
   private ItemStack insertAnyAmount(@Nonnull ItemStack stack, boolean simulate) {
      int slotCount = this.size();
      IntArrayList emptySlots = new IntArrayList(slotCount);

      for (int slot = 0; slot < this.size(); slot++) {
         if (this.isEmpty(slot)) {
            emptySlots.add(slot);
         } else {
            stack = this.insert(slot, stack, simulate);
            if (stack.isEmpty()) {
               return StackUtil.EMPTY;
            }
         }
      }

      for (int slot : emptySlots.toIntArray()) {
         stack = this.insert(slot, stack, simulate);
         if (stack.isEmpty()) {
            return StackUtil.EMPTY;
         }
      }

      return stack;
   }

   @Nonnull
   private ItemStack insertAllAtOnce(@Nonnull ItemStack stack, boolean simulate) {
      ItemStack before = asValid(stack);
      IntArrayList insertedSlots = new IntArrayList(this.size());
      IntArrayList emptySlots = new IntArrayList(this.size());

      for (int slot = 0; slot < this.size(); slot++) {
         if (this.isEmpty(slot)) {
            emptySlots.add(slot);
         } else {
            stack = this.insert(slot, stack, true);
            insertedSlots.add(slot);
            if (stack.isEmpty()) {
               break;
            }
         }
      }

      for (int slot : emptySlots.toIntArray()) {
         stack = this.insert(slot, stack, true);
         insertedSlots.add(slot);
         if (stack.isEmpty()) {
            break;
         }
      }

      if (!stack.isEmpty()) {
         return stack;
      }

      if (simulate) {
         return StackUtil.EMPTY;
      }

      for (int slot : insertedSlots.toIntArray()) {
         before = this.insert(slot, before, false);
      }

      if (!before.isEmpty()) {
         throw new IllegalStateException("Somehow inserting a lot of items at once failed when we thought it shouldn't! (" + this.getClass() + ")");
      } else {
         return StackUtil.EMPTY;
      }
   }

   @Override
   public NonNullList<ItemStack> insert(NonNullList<ItemStack> stacks, boolean simulate) {
      return stacks;
   }

   @Nonnull
   @Override
   public ItemStack extract(IStackFilter filter, int min, int max, boolean simulate) {
      if (min < 1) {
         min = 1;
      }

      if (min > max) {
         return StackUtil.EMPTY;
      }

      if (max < 0) {
         return StackUtil.EMPTY;
      }

      if (filter == null) {
         filter = StackFilter.ALL;
      }

      int slots = this.size();
      IntArrayList valids = new IntArrayList();
      int totalSize = 0;
      ItemStack toExtract = StackUtil.EMPTY;

      for (int slot = 0; slot < slots; slot++) {
         ItemStack possible = this.extract(slot, filter, 1, max - totalSize, true);
         if (!possible.isEmpty()) {
            if (toExtract.isEmpty()) {
               toExtract = possible.copy();
            }

            if (StackUtil.canMerge(toExtract, possible)) {
               totalSize += possible.getCount();
               valids.add(slot);
               if (totalSize >= max) {
                  break;
               }
            }
         }
      }

      ItemStack total = StackUtil.EMPTY;
      if (min <= totalSize) {
         for (int slot : valids.toIntArray()) {
            ItemStack extracted = this.extract(slot, filter, 1, max - total.getCount(), simulate);
            if (total.isEmpty()) {
               total = extracted.copy();
            } else {
               total.grow(extracted.getCount());
            }
         }
      }

      return total;
   }

   @Override
   public boolean canFullyAccept(@Nonnull ItemStack stack) {
      if (stack.isEmpty()) {
         return true;
      }

      ItemStack leftover = this.insert(stack.copy(), true, true);
      return leftover.isEmpty();
   }

   @Override
   public String toString() {
      ItemStack[] stacks = new ItemStack[this.size()];

      for (int i = 0; i < stacks.length; i++) {
         stacks[i] = this.extract(i, StackFilter.ALL, 1, Integer.MAX_VALUE, true);
      }

      return Arrays.toString(stacks);
   }
}

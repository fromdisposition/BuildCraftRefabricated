/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.tile;

import buildcraft.lib.misc.StackUtil;
import javax.annotation.Nonnull;
import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface StackInsertionFunction {
   @Nonnull
   StackInsertionFunction.InsertionResult modifyForInsertion(int var1, @Nonnull ItemStack var2, @Nonnull ItemStack var3);

   static StackInsertionFunction getInsertionFunction(int maxStackSize) {
      return (slot, addingTo, toInsert) -> {
         if (toInsert.isEmpty()) {
            return new StackInsertionFunction.InsertionResult(addingTo, StackUtil.EMPTY);
         }

         if (addingTo.isEmpty()) {
            int maxSize = Math.min(maxStackSize, toInsert.getMaxStackSize());
            if (toInsert.getCount() <= maxSize) {
               return new StackInsertionFunction.InsertionResult(toInsert, StackUtil.EMPTY);
            }

            ItemStack inserted = toInsert.split(maxSize);
            return new StackInsertionFunction.InsertionResult(inserted, toInsert);
         } else {
            int maxSize = Math.min(maxStackSize, addingTo.getMaxStackSize());
            // Reject at OR OVER the limit, matching the merge below: testing only "== maxStackSize" let an
            // over-full slot fall through and shrink the stored stack, handing back more items than it received.
            if (addingTo.getCount() >= maxSize || !StackUtil.canMerge(addingTo, toInsert)) {
               return new StackInsertionFunction.InsertionResult(addingTo, toInsert);
            }

            ItemStack complete = addingTo.copy();
            int count = addingTo.getCount() + toInsert.getCount();
            if (count <= maxSize) {
               complete.setCount(count);
               return new StackInsertionFunction.InsertionResult(complete, StackUtil.EMPTY);
            } else {
               complete.setCount(maxSize);
               ItemStack leftOver = toInsert.copy();
               leftOver.setCount(count - maxSize);
               return new StackInsertionFunction.InsertionResult(complete, leftOver);
            }
         }
      };
   }

   static StackInsertionFunction getDefaultInserter() {
      return getInsertionFunction(Integer.MAX_VALUE);
   }

   class InsertionResult {
      public static final StackInsertionFunction.InsertionResult EMPTY_STACKS = new StackInsertionFunction.InsertionResult(StackUtil.EMPTY, StackUtil.EMPTY);
      @Nonnull
      public final ItemStack toSet;
      @Nonnull
      public final ItemStack toReturn;

      public InsertionResult(@Nonnull ItemStack toSet, @Nonnull ItemStack toReturn) {
         this.toSet = toSet;
         this.toReturn = toReturn;
      }
   }
}

/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.tile.item;

import net.minecraft.resources.Identifier;

import javax.annotation.Nonnull;

import net.minecraft.world.item.ItemStack;

import buildcraft.lib.misc.StackUtil;

@FunctionalInterface
public interface StackInsertionFunction {

    @Nonnull
    InsertionResult modifyForInsertion(int slot, @Nonnull ItemStack addingTo, @Nonnull ItemStack toInsert);

    static StackInsertionFunction getInsertionFunction(int maxStackSize) {
        return (slot, addingTo, toInsert) -> {
            if (toInsert.isEmpty()) {
                return new InsertionResult(addingTo, StackUtil.EMPTY);
            }

            if (addingTo.isEmpty()) {
                int maxSize = Math.min(maxStackSize, toInsert.getMaxStackSize());
                if (toInsert.getCount() <= maxSize) {
                    return new InsertionResult(toInsert, StackUtil.EMPTY);
                } else {
                    ItemStack inserted = toInsert.split(maxSize);
                    return new InsertionResult(inserted, toInsert);
                }
            } else if (addingTo.getCount() == maxStackSize) {
                return new InsertionResult(addingTo, toInsert);
            } else if (StackUtil.canMerge(addingTo, toInsert)) {
                ItemStack complete = addingTo.copy();
                int count = addingTo.getCount() + toInsert.getCount();
                int maxSize = Math.min(maxStackSize, complete.getMaxStackSize());
                if (count <= maxSize) {
                    complete.setCount(count);
                    return new InsertionResult(complete, StackUtil.EMPTY);
                } else {
                    complete.setCount(maxSize);
                    ItemStack leftOver = toInsert.copy();
                    leftOver.setCount(count - maxSize);
                    return new InsertionResult(complete, leftOver);
                }
            }
            return new InsertionResult(addingTo, toInsert);
        };
    }

    public static StackInsertionFunction getDefaultInserter() {
        return getInsertionFunction(Integer.MAX_VALUE);
    }

    class InsertionResult {
        public static final InsertionResult EMPTY_STACKS = new InsertionResult(StackUtil.EMPTY, StackUtil.EMPTY);

        @Nonnull
        public final ItemStack toSet, toReturn;

        public InsertionResult(@Nonnull ItemStack toSet, @Nonnull ItemStack toReturn) {
            this.toSet = toSet;
            this.toReturn = toReturn;
        }
    }
}

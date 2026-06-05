/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package buildcraft.lib.transfer.item;

import net.minecraft.world.item.ItemStack;
import buildcraft.lib.transfer.ResourceHandler;
import buildcraft.lib.transfer.transaction.Transaction;
import buildcraft.lib.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public final class ItemUtil {
    private ItemUtil() {}

    public static ItemStack getStack(ResourceHandler<ItemResource> handler, int index) {
        var resource = handler.getResource(index);
        if (resource.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return resource.toStack(handler.getAmountAsInt(index));
    }

    public static ItemStack insertItemReturnRemaining(
            ResourceHandler<ItemResource> handler,
            ItemStack stack,
            boolean simulate,
            @Nullable TransactionContext transaction) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        try (var tx = Transaction.open(transaction)) {
            int inserted = handler.insert(ItemResource.of(stack), stack.getCount(), tx);
            if (!simulate) {
                tx.commit();
            }
            int leftover = stack.getCount() - inserted;
            return leftover == 0 ? ItemStack.EMPTY : stack.copyWithCount(leftover);
        }
    }

    public static ItemStack insertItemReturnRemaining(
            ResourceHandler<ItemResource> handler,
            int index,
            ItemStack stack,
            boolean simulate,
            @Nullable TransactionContext transaction) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        try (var tx = Transaction.open(transaction)) {
            int inserted = handler.insert(index, ItemResource.of(stack), stack.getCount(), tx);
            if (!simulate) {
                tx.commit();
            }
            int leftover = stack.getCount() - inserted;
            return leftover == 0 ? ItemStack.EMPTY : stack.copyWithCount(leftover);
        }
    }
}

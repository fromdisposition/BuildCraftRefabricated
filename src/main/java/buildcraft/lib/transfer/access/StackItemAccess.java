/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package buildcraft.lib.transfer.access;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import buildcraft.lib.transfer.ResourceHandler;
import buildcraft.lib.transfer.item.ItemResource;
import buildcraft.lib.transfer.item.VanillaContainerWrapper;
import buildcraft.lib.transfer.transaction.TransactionContext;

class StackItemAccess implements ItemAccess {
    private final Item item;

    private final ResourceHandler<ItemResource> wrapper;

    public StackItemAccess(ItemStack stack) {
        item = stack.getItem();
        wrapper = VanillaContainerWrapper.of(new SimpleContainer(stack) {
            @Override
            public void setItem(int slot, ItemStack newStack) {
                getItems().set(slot, newStack);
            }
        });
    }

    @Override
    public ItemResource getResource() {
        return wrapper.getResource(0);
    }

    @Override
    public int getAmount() {
        return wrapper.getAmountAsInt(0);
    }

    @Override
    public int insert(ItemResource resource, int amount, TransactionContext transaction) {
        if (!resource.is(this.item)) {

            return 0;
        }
        return wrapper.insert(resource, amount, transaction);
    }

    @Override
    public int extract(ItemResource resource, int amount, TransactionContext transaction) {
        return wrapper.extract(resource, amount, transaction);
    }
}

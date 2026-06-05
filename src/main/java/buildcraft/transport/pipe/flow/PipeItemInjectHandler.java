/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.flow;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

import buildcraft.api.transport.pipe.IFlowItems;
import buildcraft.lib.transfer.ResourceHandler;
import buildcraft.lib.transfer.item.ItemResource;
import buildcraft.lib.transfer.transaction.TransactionContext;

public final class PipeItemInjectHandler implements ResourceHandler<ItemResource> {
    private static final double DEFAULT_SPEED = 0.25;

    private final IFlowItems flow;
    private final Direction side;

    public PipeItemInjectHandler(IFlowItems flow, Direction side) {
        this.flow = flow;
        this.side = side;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public ItemResource getResource(int index) {
        return ItemResource.EMPTY;
    }

    @Override
    public long getAmountAsLong(int index) {
        return 0;
    }

    @Override
    public long getCapacityAsLong(int index, ItemResource resource) {
        return resource.isEmpty() ? 0 : resource.getMaxStackSize();
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return index == 0;
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        if (index != 0 || resource.isEmpty() || amount <= 0 || !flow.canInjectItems(side)) {
            return 0;
        }
        ItemStack stack = resource.toStack(amount);
        ItemStack remaining = flow.injectItem(stack, true, side, null, DEFAULT_SPEED);
        return amount - remaining.getCount();
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        return 0;
    }
}

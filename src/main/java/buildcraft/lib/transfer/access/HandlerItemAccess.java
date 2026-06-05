/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package buildcraft.lib.transfer.access;

import java.util.Objects;
import buildcraft.lib.transfer.ResourceHandler;
import buildcraft.lib.transfer.TransferPreconditions;
import buildcraft.lib.transfer.item.ItemResource;
import buildcraft.lib.transfer.transaction.TransactionContext;

public class HandlerItemAccess implements ItemAccess {
    protected final ResourceHandler<ItemResource> handler;
    protected final int index;

    public HandlerItemAccess(ResourceHandler<ItemResource> handler, int index) {
        Objects.checkIndex(index, handler.size());
        this.handler = handler;
        this.index = index;
    }

    @Override
    public ItemResource getResource() {
        return handler.getResource(index);
    }

    @Override
    public int getAmount() {
        return handler.getAmountAsInt(index);
    }

    @Override
    public int insert(ItemResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);

        int inserted = handler.insert(index, resource, amount, transaction);
        if (inserted < amount) {

            inserted += handler.insert(resource, amount - inserted, transaction);
        }
        return inserted;
    }

    @Override
    public int extract(ItemResource resource, int amount, TransactionContext transaction) {
        return handler.extract(index, resource, amount, transaction);
    }
}

/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package buildcraft.lib.transfer;

import java.util.SequencedCollection;
import buildcraft.lib.transfer.resource.Resource;
import buildcraft.lib.transfer.transaction.TransactionContext;

public class CombinedResourceHandler<T extends Resource> implements ResourceHandler<T> {

    private final ResourceHandler<T>[] handlers;

    private final int[] baseIndex;

    private final int sizeCache;

    @SuppressWarnings("unchecked")
    public CombinedResourceHandler(SequencedCollection<? extends ResourceHandler<T>> handlers) {
        this(handlers.toArray(ResourceHandler[]::new));
    }

    @SafeVarargs
    public CombinedResourceHandler(ResourceHandler<T>... handlers) {
        this.handlers = handlers;
        this.baseIndex = new int[handlers.length];
        int index = 0;
        for (int i = 0; i < handlers.length; i++) {
            this.baseIndex[i] = index;
            index += handlers[i].size();
        }
        this.sizeCache = index;
    }

    protected int getHandlerIndex(int index) {
        if (index < 0 || index >= sizeCache)
            throw new IndexOutOfBoundsException("Index " + index + " is out-of-bounds for combined handler with size " + sizeCache);

        for (int handlerIndex = 0; handlerIndex < baseIndex.length - 1; handlerIndex++) {
            if (index < baseIndex[handlerIndex + 1]) {
                return handlerIndex;
            }
        }

        return baseIndex.length - 1;
    }

    protected ResourceHandler<T> getHandlerFromIndex(int handlerIndex) {
        return handlers[handlerIndex];
    }

    protected int getSlotFromIndex(int index, int handlerIndex) {
        return index - baseIndex[handlerIndex];
    }

    @Override
    public final int size() {
        return sizeCache;
    }

    @Override
    public T getResource(int index) {
        int handlerIndex = getHandlerIndex(index);
        return getHandlerFromIndex(handlerIndex).getResource(getSlotFromIndex(index, handlerIndex));
    }

    @Override
    public long getAmountAsLong(int index) {
        int handlerIndex = getHandlerIndex(index);
        return getHandlerFromIndex(handlerIndex).getAmountAsLong(getSlotFromIndex(index, handlerIndex));
    }

    @Override
    public long getCapacityAsLong(int index, T resource) {
        int handlerIndex = getHandlerIndex(index);
        return getHandlerFromIndex(handlerIndex).getCapacityAsLong(getSlotFromIndex(index, handlerIndex), resource);
    }

    @Override
    public boolean isValid(int index, T resource) {
        int handlerIndex = getHandlerIndex(index);
        if (resource.isEmpty()) return true;
        return getHandlerFromIndex(handlerIndex).isValid(getSlotFromIndex(index, handlerIndex), resource);
    }

    @Override
    public int insert(int index, T resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);

        int handlerIndex = getHandlerIndex(index);
        return getHandlerFromIndex(handlerIndex).insert(getSlotFromIndex(index, handlerIndex), resource, amount, transaction);
    }

    @Override
    public int insert(T resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);

        int inserted = 0;
        for (ResourceHandler<T> resourceHandler : handlers) {
            inserted += resourceHandler.insert(resource, amount - inserted, transaction);
            if (inserted == amount) break;
        }
        return inserted;
    }

    @Override
    public int extract(int index, T resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);

        int handlerIndex = getHandlerIndex(index);
        return getHandlerFromIndex(handlerIndex).extract(getSlotFromIndex(index, handlerIndex), resource, amount, transaction);
    }

    @Override
    public int extract(T resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);

        int extracted = 0;
        for (ResourceHandler<T> resourceHandler : handlers) {
            extracted += resourceHandler.extract(resource, amount - extracted, transaction);
            if (extracted == amount) break;
        }
        return extracted;
    }
}

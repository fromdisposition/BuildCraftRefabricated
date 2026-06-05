/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package buildcraft.lib.transfer;

import java.util.Objects;
import java.util.function.Supplier;
import buildcraft.lib.transfer.resource.Resource;
import buildcraft.lib.transfer.transaction.TransactionContext;

public class RangedResourceHandler<T extends Resource> extends DelegatingResourceHandler<T> {

    public static <T extends Resource> RangedResourceHandler<T> of(ResourceHandler<T> delegate, int start, int end) {
        return new RangedResourceHandler<>(delegate, start, end);
    }

    public static <T extends Resource> RangedResourceHandler<T> of(Supplier<ResourceHandler<T>> delegate, int start, int end) {
        return new RangedResourceHandler<>(delegate, start, end);
    }

    public static <T extends Resource> RangedResourceHandler<T> ofSingleIndex(ResourceHandler<T> delegate, int index) {
        return new RangedResourceHandler<>(delegate, index, index + 1);
    }

    public static <T extends Resource> RangedResourceHandler<T> ofSingleIndex(Supplier<ResourceHandler<T>> delegate, int index) {
        return new RangedResourceHandler<>(delegate, index, index + 1);
    }

    protected int start;
    protected int end;

    protected RangedResourceHandler(ResourceHandler<T> delegate, int start, int end) {
        this(() -> delegate, start, end);
    }

    protected RangedResourceHandler(Supplier<ResourceHandler<T>> delegate, int start, int end) {
        super(delegate);
        if (start < 0 || start >= end) {
            throw new IndexOutOfBoundsException("Invalid range: start=" + start + ", end=" + end);
        }
        int delegateSize = delegate.get().size();
        if (end > delegateSize) {
            throw new IndexOutOfBoundsException("Invalid range: end " + end + " is larger than the size of the handler " + delegateSize);
        }
        this.start = start;
        this.end = end;
    }

    @Override
    public int size() {
        return end - start;
    }

    @Override
    protected int convertIndex(int index) {
        Objects.checkIndex(index, size());
        return index + start;
    }

    @Override
    public int extract(T resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);

        int extracted = 0;
        ResourceHandler<T> handler = getDelegate();
        for (int index = start; index < end; index++) {
            extracted += handler.extract(index, resource, amount - extracted, transaction);
            if (extracted == amount) {
                return extracted;
            }
        }

        return extracted;
    }

    @Override
    public int insert(T resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);

        int inserted = 0;
        ResourceHandler<T> handler = getDelegate();
        for (int index = start; index < end; index++) {
            inserted += handler.insert(index, resource, amount - inserted, transaction);
            if (inserted == amount) {
                return inserted;
            }
        }

        return inserted;
    }
}

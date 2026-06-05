/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package buildcraft.lib.transfer;

import java.util.Objects;
import buildcraft.lib.transfer.access.ItemAccess;
import buildcraft.lib.transfer.item.ItemResource;
import buildcraft.lib.transfer.resource.Resource;
import buildcraft.lib.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public abstract class ItemAccessResourceHandler<T extends Resource> implements ResourceHandler<T> {
    protected final ItemAccess itemAccess;
    protected final int size;

    protected ItemAccessResourceHandler(ItemAccess itemAccess, int size) {
        this.itemAccess = itemAccess;
        this.size = size;
    }

    protected abstract T getResourceFrom(ItemResource accessResource, int index);

    protected abstract int getAmountFrom(ItemResource accessResource, int index);

    @Nullable
    protected abstract ItemResource update(ItemResource accessResource, int index, T newResource, int newAmount);

    @Override
    public boolean isValid(int index, T resource) {
        return true;
    }

    protected abstract int getCapacity(int index, T resource);

    @Override
    public int size() {
        return size;
    }

    @Override
    public T getResource(int index) {
        Objects.checkIndex(index, size());
        return getResourceFrom(itemAccess.getResource(), index);
    }

    @Override
    public long getAmountAsLong(int index) {
        Objects.checkIndex(index, size());

        return (long) itemAccess.getAmount() * getAmountFrom(itemAccess.getResource(), index);
    }

    @Override
    public long getCapacityAsLong(int index, T resource) {
        Objects.checkIndex(index, size());
        if (resource.isEmpty() || isValid(index, resource)) {

            return (long) itemAccess.getAmount() * getCapacity(index, resource);
        }
        return 0;
    }

    @Override
    public int insert(int index, T resource, int amount, TransactionContext transaction) {
        Objects.checkIndex(index, size());
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);

        int accessAmount = itemAccess.getAmount();
        if (accessAmount == 0) {
            return 0;
        }
        int amountPerItem = amount / accessAmount;

        ItemResource accessResource = itemAccess.getResource();
        int currentAmountPerItem = getAmountFrom(accessResource, index);

        if ((currentAmountPerItem == 0 || resource.equals(getResourceFrom(accessResource, index))) && isValid(index, resource)) {
            int insertedPerItem = Math.min(amountPerItem, getCapacity(index, resource) - currentAmountPerItem);

            if (insertedPerItem > 0) {
                ItemResource filledResource = update(accessResource, index, resource, insertedPerItem + currentAmountPerItem);

                if (filledResource == null) {
                    return insertedPerItem * itemAccess.extract(itemAccess.getResource(), accessAmount, transaction);
                } else if (!filledResource.isEmpty()) {
                    return insertedPerItem * itemAccess.exchange(filledResource, accessAmount, transaction);
                }
            }
        }

        return 0;
    }

    @Override
    public int extract(int index, T resource, int amount, TransactionContext transaction) {
        Objects.checkIndex(index, size());
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);

        int accessAmount = itemAccess.getAmount();
        if (accessAmount == 0) {
            return 0;
        }

        ItemResource accessResource = itemAccess.getResource();
        T currentResource = getResourceFrom(accessResource, index);

        if (resource.equals(currentResource)) {
            int currentAmountPerItem = getAmountFrom(accessResource, index);
            int extractedPerItem = Math.min(amount / accessAmount, currentAmountPerItem);

            if (extractedPerItem > 0) {
                ItemResource emptiedResource = update(accessResource, index, resource, currentAmountPerItem - extractedPerItem);

                if (emptiedResource == null) {
                    return extractedPerItem * itemAccess.extract(itemAccess.getResource(), accessAmount, transaction);
                } else if (!emptiedResource.isEmpty()) {
                    return extractedPerItem * itemAccess.exchange(emptiedResource, accessAmount, transaction);
                }
            }
        }

        return 0;
    }
}

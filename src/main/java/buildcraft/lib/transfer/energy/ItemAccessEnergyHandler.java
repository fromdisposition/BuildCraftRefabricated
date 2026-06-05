/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package buildcraft.lib.transfer.energy;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;
import buildcraft.lib.attachments.IAttachmentProvider;
import buildcraft.lib.attachments.RegisterAttachmentsEvent;
import buildcraft.lib.transfer.TransferPreconditions;
import buildcraft.lib.transfer.access.ItemAccess;
import buildcraft.lib.transfer.item.ItemResource;
import buildcraft.lib.transfer.transaction.TransactionContext;

public class ItemAccessEnergyHandler implements EnergyHandler {
    protected final ItemAccess itemAccess;
    protected final Item validItem;
    protected final DataComponentType<Integer> energyComponent;
    protected final int capacity;
    protected final int maxInsert;
    protected final int maxExtract;

    public ItemAccessEnergyHandler(ItemAccess itemAccess, DataComponentType<Integer> energyComponent, int capacity) {
        this(itemAccess, energyComponent, capacity, capacity);
    }

    public ItemAccessEnergyHandler(ItemAccess itemAccess, DataComponentType<Integer> energyComponent, int capacity, int maxTransfer) {
        this(itemAccess, energyComponent, capacity, maxTransfer, maxTransfer);
    }

    public ItemAccessEnergyHandler(ItemAccess itemAccess, DataComponentType<Integer> energyComponent, int capacity, int maxInsert, int maxExtract) {
        TransferPreconditions.checkNonNegative(capacity);
        TransferPreconditions.checkNonNegative(maxInsert);
        TransferPreconditions.checkNonNegative(maxExtract);

        this.itemAccess = itemAccess;

        this.validItem = itemAccess.getResource().getItem();
        this.energyComponent = energyComponent;
        this.capacity = capacity;
        this.maxInsert = maxInsert;
        this.maxExtract = maxExtract;
    }

    protected int getAmountFrom(ItemResource accessResource) {
        if (!accessResource.is(validItem)) {
            return 0;
        }
        return accessResource.getOrDefault(energyComponent, 0);
    }

    protected ItemResource update(ItemResource accessResource, int newAmount) {
        return accessResource.with(energyComponent, newAmount);
    }

    @Override
    public long getAmountAsLong() {

        return (long) itemAccess.getAmount() * getAmountFrom(itemAccess.getResource());
    }

    @Override
    public long getCapacityAsLong() {
        var accessResource = itemAccess.getResource();
        if (!accessResource.is(validItem)) {
            return 0;
        }

        return (long) itemAccess.getAmount() * this.capacity;
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonNegative(amount);

        int accessAmount = itemAccess.getAmount();
        if (accessAmount == 0) {
            return 0;
        }
        int amountPerItem = Math.min(maxInsert, amount / accessAmount);
        if (amountPerItem == 0) {
            return 0;
        }

        ItemResource accessResource = itemAccess.getResource();
        if (!accessResource.is(validItem)) {
            return 0;
        }
        int currentAmountPerItem = getAmountFrom(accessResource);

        int insertedPerItem = Math.min(amountPerItem, capacity - currentAmountPerItem);
        if (insertedPerItem > 0) {
            ItemResource filledResource = update(accessResource, currentAmountPerItem + insertedPerItem);

            if (!filledResource.isEmpty()) {
                return insertedPerItem * itemAccess.exchange(filledResource, accessAmount, transaction);
            }
        }

        return 0;
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonNegative(amount);

        int accessAmount = itemAccess.getAmount();
        if (accessAmount == 0) {
            return 0;
        }
        int amountPerItem = Math.min(maxExtract, amount / accessAmount);
        if (amountPerItem == 0) {
            return 0;
        }

        ItemResource accessResource = itemAccess.getResource();

        int currentAmountPerItem = getAmountFrom(accessResource);

        int extractedPerItem = Math.min(amountPerItem, currentAmountPerItem);
        if (extractedPerItem > 0) {
            ItemResource emptiedResource = update(accessResource, currentAmountPerItem - extractedPerItem);

            if (!emptiedResource.isEmpty()) {
                return extractedPerItem * itemAccess.exchange(emptiedResource, accessAmount, transaction);
            }
        }

        return 0;
    }
}

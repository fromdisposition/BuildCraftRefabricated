/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.fabric.transfer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import buildcraft.lib.transfer.ResourceHandler;
import buildcraft.lib.transfer.item.ItemResource;

public final class BcItemStorage implements Storage<ItemVariant> {
    private final ResourceHandler<ItemResource> handler;

    public BcItemStorage(ResourceHandler<ItemResource> handler) {
        this.handler = handler;
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        if (resource.isBlank() || maxAmount <= 0) {
            return 0;
        }
        buildcraft.lib.transfer.transaction.TransactionContext bcTx = FabricToBcTransactionMirror.enlist(transaction);
        return handler.insert(TransferConvert.toItemResource(resource), saturate(maxAmount), bcTx);
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        if (resource.isBlank() || maxAmount <= 0) {
            return 0;
        }
        buildcraft.lib.transfer.transaction.TransactionContext bcTx = FabricToBcTransactionMirror.enlist(transaction);
        return handler.extract(TransferConvert.toItemResource(resource), saturate(maxAmount), bcTx);
    }

    @Override
    public Iterator<StorageView<ItemVariant>> iterator() {
        int size = handler.size();
        List<StorageView<ItemVariant>> views = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            views.add(new View(i));
        }
        return views.iterator();
    }

    private static int saturate(long amount) {
        return amount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) amount;
    }

    private final class View implements StorageView<ItemVariant> {
        private final int index;

        private View(int index) {
            this.index = index;
        }

        @Override
        public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            if (resource.isBlank() || maxAmount <= 0) {
                return 0;
            }
            buildcraft.lib.transfer.transaction.TransactionContext bcTx = FabricToBcTransactionMirror.enlist(transaction);
            return handler.extract(index, TransferConvert.toItemResource(resource), saturate(maxAmount), bcTx);
        }

        @Override
        public boolean isResourceBlank() {
            return handler.getResource(index).isEmpty();
        }

        @Override
        public ItemVariant getResource() {
            ItemResource resource = handler.getResource(index);
            return resource.isEmpty() ? ItemVariant.blank() : TransferConvert.toVariant(resource);
        }

        @Override
        public long getAmount() {
            return handler.getAmountAsLong(index);
        }

        @Override
        public long getCapacity() {
            return handler.getCapacityAsLong(index, handler.getResource(index));
        }
    }
}

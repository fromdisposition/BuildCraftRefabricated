/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.fabric.transfer;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

import buildcraft.lib.transfer.ResourceHandler;
import buildcraft.lib.transfer.item.ItemResource;
import buildcraft.lib.transfer.transaction.FabricTransactionMirror;
import buildcraft.lib.transfer.transaction.TransactionContext;

public final class FabricItemResourceHandler implements ResourceHandler<ItemResource> {
    private final Storage<ItemVariant> storage;
    private List<StorageView<ItemVariant>> viewCache;

    public FabricItemResourceHandler(Storage<ItemVariant> storage) {
        this.storage = storage;
    }

    private List<StorageView<ItemVariant>> views() {
        if (viewCache == null) {
            viewCache = new ArrayList<>();
            for (StorageView<ItemVariant> view : storage) {
                viewCache.add(view);
            }
        }
        return viewCache;
    }

    @Override
    public int size() {
        return Math.max(1, views().size());
    }

    @Override
    public ItemResource getResource(int index) {
        List<StorageView<ItemVariant>> vs = views();
        if (index < 0 || index >= vs.size()) {
            return ItemResource.EMPTY;
        }
        return TransferConvert.toItemResource(vs.get(index).getResource());
    }

    @Override
    public long getAmountAsLong(int index) {
        List<StorageView<ItemVariant>> vs = views();
        if (index < 0 || index >= vs.size()) {
            return 0;
        }
        return vs.get(index).getAmount();
    }

    @Override
    public long getCapacityAsLong(int index, ItemResource resource) {
        List<StorageView<ItemVariant>> vs = views();
        if (index < 0 || index >= vs.size()) {
            return 0;
        }
        return vs.get(index).getCapacity();
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return true;
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {

        if (index != 0) {
            return 0;
        }
        return insert(resource, amount, transaction);
    }

    @Override
    public int insert(ItemResource resource, int amount, TransactionContext transaction) {
        if (resource.isEmpty() || amount <= 0 || !storage.supportsInsertion()) {
            return 0;
        }
        ItemVariant variant = TransferConvert.toVariant(resource);
        Transaction fabricTx = FabricTransactionMirror.enlist(transaction);
        return (int) storage.insert(variant, amount, fabricTx);
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        List<StorageView<ItemVariant>> vs = views();
        if (index < 0 || index >= vs.size() || resource.isEmpty() || amount <= 0) {
            return 0;
        }
        ItemVariant variant = TransferConvert.toVariant(resource);
        Transaction fabricTx = FabricTransactionMirror.enlist(transaction);
        return (int) vs.get(index).extract(variant, amount, fabricTx);
    }

    @Override
    public int extract(ItemResource resource, int amount, TransactionContext transaction) {
        if (resource.isEmpty() || amount <= 0 || !storage.supportsExtraction()) {
            return 0;
        }
        ItemVariant variant = TransferConvert.toVariant(resource);
        Transaction fabricTx = FabricTransactionMirror.enlist(transaction);
        return (int) storage.extract(variant, amount, fabricTx);
    }
}

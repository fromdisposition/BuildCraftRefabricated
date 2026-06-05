/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.fabric.transfer;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

import buildcraft.lib.transfer.ResourceHandler;
import buildcraft.lib.transfer.fluid.FluidResource;
import buildcraft.lib.transfer.transaction.FabricTransactionMirror;
import buildcraft.lib.transfer.transaction.TransactionContext;

public final class FabricFluidResourceHandler implements ResourceHandler<FluidResource> {
    private final Storage<FluidVariant> storage;

    public FabricFluidResourceHandler(Storage<FluidVariant> storage) {
        this.storage = storage;
    }

    private List<StorageView<FluidVariant>> views() {
        List<StorageView<FluidVariant>> list = new ArrayList<>();
        for (StorageView<FluidVariant> view : storage) {
            list.add(view);
        }
        return list;
    }

    @Override
    public int size() {

        return Math.max(1, views().size());
    }

    @Override
    public FluidResource getResource(int index) {
        List<StorageView<FluidVariant>> vs = views();
        if (index < 0 || index >= vs.size()) {
            return FluidResource.EMPTY;
        }
        return TransferConvert.toFluidResource(vs.get(index).getResource());
    }

    @Override
    public long getAmountAsLong(int index) {
        List<StorageView<FluidVariant>> vs = views();
        if (index < 0 || index >= vs.size()) {
            return 0;
        }
        return TransferConvert.dropletsToMb(vs.get(index).getAmount());
    }

    @Override
    public long getCapacityAsLong(int index, FluidResource resource) {
        List<StorageView<FluidVariant>> vs = views();
        if (index < 0 || index >= vs.size()) {
            return 0;
        }
        return TransferConvert.dropletsToMb(vs.get(index).getCapacity());
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {

        return true;
    }

    @Override
    public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {

        if (index != 0) {
            return 0;
        }
        return insert(resource, amount, transaction);
    }

    @Override
    public int insert(FluidResource resource, int amount, TransactionContext transaction) {
        if (resource.isEmpty() || amount <= 0 || !storage.supportsInsertion()) {
            return 0;
        }
        FluidVariant variant = TransferConvert.toVariant(resource);
        Transaction fabricTx = FabricTransactionMirror.enlist(transaction);
        long requested = TransferConvert.mbToDroplets(amount);

        long accepted;
        try (Transaction probe = Transaction.openNested(fabricTx)) {
            accepted = storage.insert(variant, requested, probe);

        }
        long wholeMb = TransferConvert.dropletsToMb(accepted);
        if (wholeMb <= 0) {
            return 0;
        }
        long moved = storage.insert(variant, TransferConvert.mbToDroplets(wholeMb), fabricTx);
        return (int) TransferConvert.dropletsToMb(moved);
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
        List<StorageView<FluidVariant>> vs = views();
        if (index < 0 || index >= vs.size() || resource.isEmpty() || amount <= 0) {
            return 0;
        }
        StorageView<FluidVariant> view = vs.get(index);
        return extractWholeMb(view::extract, resource, amount, transaction);
    }

    @Override
    public int extract(FluidResource resource, int amount, TransactionContext transaction) {
        if (resource.isEmpty() || amount <= 0 || !storage.supportsExtraction()) {
            return 0;
        }
        return extractWholeMb(storage::extract, resource, amount, transaction);
    }

    private int extractWholeMb(FabricExtract source, FluidResource resource, int amount, TransactionContext transaction) {
        FluidVariant variant = TransferConvert.toVariant(resource);
        Transaction fabricTx = FabricTransactionMirror.enlist(transaction);
        long requested = TransferConvert.mbToDroplets(amount);

        long available;
        try (Transaction probe = Transaction.openNested(fabricTx)) {
            available = source.extract(variant, requested, probe);
        }
        long wholeMb = TransferConvert.dropletsToMb(available);
        if (wholeMb <= 0) {
            return 0;
        }
        long moved = source.extract(variant, TransferConvert.mbToDroplets(wholeMb), fabricTx);
        return (int) TransferConvert.dropletsToMb(moved);
    }

    @FunctionalInterface
    private interface FabricExtract {
        long extract(FluidVariant resource, long maxAmount, Transaction transaction);
    }
}

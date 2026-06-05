/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.fabric.transfer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import buildcraft.lib.transfer.ResourceHandler;
import buildcraft.lib.transfer.fluid.FluidResource;

public final class BcFluidStorage implements Storage<FluidVariant> {
    private final ResourceHandler<FluidResource> handler;

    public BcFluidStorage(ResourceHandler<FluidResource> handler) {
        this.handler = handler;
    }

    @Override
    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        if (resource.isBlank() || maxAmount <= 0) {
            return 0;
        }
        long millibuckets = TransferConvert.dropletsToMb(maxAmount);
        if (millibuckets <= 0) {
            return 0;
        }
        buildcraft.lib.transfer.transaction.TransactionContext bcTx = FabricToBcTransactionMirror.enlist(transaction);
        int inserted = handler.insert(TransferConvert.toFluidResource(resource), (int) millibuckets, bcTx);
        return TransferConvert.mbToDroplets(inserted);
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        if (resource.isBlank() || maxAmount <= 0) {
            return 0;
        }
        long millibuckets = TransferConvert.dropletsToMb(maxAmount);
        if (millibuckets <= 0) {
            return 0;
        }
        buildcraft.lib.transfer.transaction.TransactionContext bcTx = FabricToBcTransactionMirror.enlist(transaction);
        int extracted = handler.extract(TransferConvert.toFluidResource(resource), (int) millibuckets, bcTx);
        return TransferConvert.mbToDroplets(extracted);
    }

    @Override
    public Iterator<StorageView<FluidVariant>> iterator() {
        int size = handler.size();
        List<StorageView<FluidVariant>> views = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            views.add(new View(i));
        }
        return views.iterator();
    }

    private final class View implements StorageView<FluidVariant> {
        private final int index;

        private View(int index) {
            this.index = index;
        }

        @Override
        public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
            if (resource.isBlank() || maxAmount <= 0) {
                return 0;
            }
            long millibuckets = TransferConvert.dropletsToMb(maxAmount);
            if (millibuckets <= 0) {
                return 0;
            }
            buildcraft.lib.transfer.transaction.TransactionContext bcTx = FabricToBcTransactionMirror.enlist(transaction);
            int extracted = handler.extract(index, TransferConvert.toFluidResource(resource), (int) millibuckets, bcTx);
            return TransferConvert.mbToDroplets(extracted);
        }

        @Override
        public boolean isResourceBlank() {
            return handler.getResource(index).isEmpty();
        }

        @Override
        public FluidVariant getResource() {
            FluidResource resource = handler.getResource(index);
            return resource.isEmpty() ? FluidVariant.blank() : TransferConvert.toVariant(resource);
        }

        @Override
        public long getAmount() {
            return TransferConvert.mbToDroplets(handler.getAmountAsLong(index));
        }

        @Override
        public long getCapacity() {
            return TransferConvert.mbToDroplets(handler.getCapacityAsLong(index, handler.getResource(index)));
        }
    }
}

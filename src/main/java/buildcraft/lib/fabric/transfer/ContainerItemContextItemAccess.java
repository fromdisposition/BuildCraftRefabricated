/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.fabric.transfer;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;

import buildcraft.lib.transfer.access.ItemAccess;
import buildcraft.lib.transfer.item.ItemResource;
import buildcraft.lib.transfer.transaction.FabricTransactionMirror;
import buildcraft.lib.transfer.transaction.TransactionContext;

public final class ContainerItemContextItemAccess implements ItemAccess {
    private final ContainerItemContext context;

    public ContainerItemContextItemAccess(ContainerItemContext context) {
        this.context = context;
    }

    public ContainerItemContext context() {
        return context;
    }

    @Override
    public ItemResource getResource() {
        ItemVariant variant = context.getItemVariant();
        return variant.isBlank() ? ItemResource.EMPTY : TransferConvert.toItemResource(variant);
    }

    @Override
    public int getAmount() {
        long amount = context.getAmount();
        return amount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) amount;
    }

    @Override
    public int insert(ItemResource resource, int amount, TransactionContext transaction) {
        if (resource.isEmpty() || amount <= 0) {
            return 0;
        }
        ItemVariant variant = TransferConvert.toVariant(resource);
        long inserted = context.insert(variant, amount, FabricTransactionMirror.enlist(transaction));
        return inserted > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) inserted;
    }

    @Override
    public int extract(ItemResource resource, int amount, TransactionContext transaction) {
        if (resource.isEmpty() || amount <= 0) {
            return 0;
        }
        ItemVariant variant = TransferConvert.toVariant(resource);
        long extracted = context.extract(variant, amount, FabricTransactionMirror.enlist(transaction));
        return extracted > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) extracted;
    }
}

/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package buildcraft.lib.transfer.energy;

import java.util.function.Supplier;
import buildcraft.lib.transfer.TransferPreconditions;
import buildcraft.lib.transfer.transaction.TransactionContext;

public class LimitingEnergyHandler extends DelegatingEnergyHandler {
    protected int maxInsert, maxExtract;

    public LimitingEnergyHandler(EnergyHandler delegate, int maxInsert, int maxExtract) {
        this(() -> delegate, maxInsert, maxExtract);
    }

    public LimitingEnergyHandler(Supplier<EnergyHandler> delegate, int maxInsert, int maxExtract) {
        super(delegate);
        TransferPreconditions.checkNonNegative(maxInsert);
        TransferPreconditions.checkNonNegative(maxExtract);

        this.maxInsert = maxInsert;
        this.maxExtract = maxExtract;
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonNegative(amount);
        int toInsert = Math.min(amount, maxInsert);
        return toInsert <= 0 ? 0 : super.insert(toInsert, transaction);
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonNegative(amount);
        int toExtract = Math.min(amount, maxExtract);
        return toExtract <= 0 ? 0 : super.extract(toExtract, transaction);
    }
}

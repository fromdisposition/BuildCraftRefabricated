/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package buildcraft.lib.transfer.energy;

import buildcraft.lib.transfer.TransferPreconditions;
import buildcraft.lib.transfer.transaction.TransactionContext;

public class VoidingEnergyHandler implements EnergyHandler {
    public static final VoidingEnergyHandler INSTANCE = new VoidingEnergyHandler();

    @Override
    public long getAmountAsLong() {
        return 0;
    }

    @Override
    public long getCapacityAsLong() {
        return Long.MAX_VALUE;
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonNegative(amount);

        return amount;
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonNegative(amount);
        return 0;
    }
}

/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package buildcraft.lib.transfer.energy;

import java.util.Objects;
import java.util.function.Supplier;
import buildcraft.lib.transfer.TransferPreconditions;
import buildcraft.lib.transfer.transaction.TransactionContext;

public class DelegatingEnergyHandler implements EnergyHandler {
    protected final Supplier<EnergyHandler> delegate;

    public DelegatingEnergyHandler(EnergyHandler delegate) {
        Objects.requireNonNull(delegate);
        this.delegate = () -> delegate;
    }

    public DelegatingEnergyHandler(Supplier<EnergyHandler> delegate) {
        this.delegate = delegate;
    }

    @Override
    public long getAmountAsLong() {
        return getDelegate().getAmountAsLong();
    }

    @Override
    public long getCapacityAsLong() {
        return getDelegate().getCapacityAsLong();
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonNegative(amount);
        return getDelegate().insert(amount, transaction);
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonNegative(amount);
        return getDelegate().extract(amount, transaction);
    }

    public EnergyHandler getDelegate() {
        return delegate.get();
    }
}

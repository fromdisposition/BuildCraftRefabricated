/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package buildcraft.lib.transfer.energy;

import com.google.common.primitives.Ints;
import buildcraft.lib.attachments.Attachments;
import buildcraft.lib.transfer.TransferPreconditions;
import buildcraft.lib.transfer.transaction.SnapshotJournal;
import buildcraft.lib.transfer.transaction.Transaction;
import buildcraft.lib.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.ApiStatus;

public interface EnergyHandler {

    long getAmountAsLong();

    @ApiStatus.NonExtendable
    default int getAmountAsInt() {
        return Ints.saturatedCast(getAmountAsLong());
    }

    long getCapacityAsLong();

    @ApiStatus.NonExtendable
    default int getCapacityAsInt() {
        return Ints.saturatedCast(getCapacityAsLong());
    }

    int insert(int amount, TransactionContext transaction);

    int extract(int amount, TransactionContext transaction);
}

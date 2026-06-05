/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package buildcraft.lib.transfer.item;

import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.SingleThreadedRandomSource;
import buildcraft.lib.transfer.transaction.SnapshotJournal;
import buildcraft.lib.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class TransactionalRandom extends SnapshotJournal<Long> {
    private long seed = RandomSupport.generateUniqueSeed();

    @Override
    protected Long createSnapshot() {
        return seed;
    }

    @Override
    protected void revertToSnapshot(Long snapshot) {
        seed = snapshot;
    }

    public double nextDouble(TransactionContext transaction) {
        updateSnapshots(transaction);
        var random = new SingleThreadedRandomSource(seed);
        double rand = random.nextDouble();
        seed = random.nextLong();
        return rand;
    }
}

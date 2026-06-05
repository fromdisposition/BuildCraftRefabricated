/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.transfer.transaction;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public final class FabricTransactionMirror extends SnapshotJournal<Void> {
    private static final ThreadLocal<FabricTransactionMirror> THREAD =
            ThreadLocal.withInitial(FabricTransactionMirror::new);

    private final List<net.fabricmc.fabric.api.transfer.v1.transaction.Transaction> fabricStack = new ArrayList<>();

    private final BitSet hooked = new BitSet();

    private FabricTransactionMirror() {}

    public static net.fabricmc.fabric.api.transfer.v1.transaction.Transaction enlist(TransactionContext bcContext) {
        return THREAD.get().enlistImpl((Transaction) bcContext);
    }

    private net.fabricmc.fabric.api.transfer.v1.transaction.Transaction enlistImpl(Transaction bc) {
        int depth = bc.depth();

        while (fabricStack.size() > depth + 1) {
            fabricStack.remove(fabricStack.size() - 1).abort();
        }

        TransactionManager manager = bc.manager;
        while (fabricStack.size() <= depth) {
            int newDepth = fabricStack.size();
            net.fabricmc.fabric.api.transfer.v1.transaction.Transaction fabricTx = newDepth == 0
                    ? net.fabricmc.fabric.api.transfer.v1.transaction.Transaction.openOuter()
                    : net.fabricmc.fabric.api.transfer.v1.transaction.Transaction.openNested(fabricStack.get(newDepth - 1));
            fabricStack.add(fabricTx);
            if (!hooked.get(newDepth)) {
                manager.getOpenTransaction(newDepth).journalsToClose.add(this);
                hooked.set(newDepth);
            }
        }
        return fabricStack.get(depth);
    }

    @Override
    void onClose(Transaction transaction, boolean wasAborted) {
        int depth = transaction.depth();
        hooked.clear(depth);

        if (fabricStack.size() != depth + 1) {

            while (fabricStack.size() > depth) {
                try {
                    fabricStack.remove(fabricStack.size() - 1).abort();
                } catch (RuntimeException ignored) {

                }
            }
            return;
        }

        net.fabricmc.fabric.api.transfer.v1.transaction.Transaction fabricTx = fabricStack.remove(fabricStack.size() - 1);
        if (wasAborted) {
            fabricTx.abort();
        } else {
            fabricTx.commit();
        }
    }

    @Override
    protected Void createSnapshot() {
        throw new UnsupportedOperationException("FabricTransactionMirror does not use snapshots");
    }

    @Override
    protected void revertToSnapshot(Void snapshot) {
        throw new UnsupportedOperationException("FabricTransactionMirror does not use snapshots");
    }
}

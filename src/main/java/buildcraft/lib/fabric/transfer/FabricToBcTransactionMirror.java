/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.fabric.transfer;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import buildcraft.lib.transfer.transaction.Transaction;

public final class FabricToBcTransactionMirror {
    private static final ThreadLocal<FabricToBcTransactionMirror> THREAD =
            ThreadLocal.withInitial(FabricToBcTransactionMirror::new);

    private final List<Transaction> bcStack = new ArrayList<>();

    private final BitSet hooked = new BitSet();

    private FabricToBcTransactionMirror() {}

    public static buildcraft.lib.transfer.transaction.TransactionContext enlist(TransactionContext fabricContext) {
        return THREAD.get().enlistImpl(fabricContext);
    }

    private buildcraft.lib.transfer.transaction.TransactionContext enlistImpl(TransactionContext fabricContext) {
        int depth = fabricContext.nestingDepth();

        while (bcStack.size() > depth + 1) {
            bcStack.remove(bcStack.size() - 1).close();
        }

        while (bcStack.size() <= depth) {
            int newDepth = bcStack.size();
            Transaction bcTx = newDepth == 0
                    ? Transaction.openRoot()
                    : Transaction.open(bcStack.get(newDepth - 1));
            bcStack.add(bcTx);
            if (!hooked.get(newDepth)) {
                final int hookedDepth = newDepth;
                TransactionContext ancestor = fabricContext.getOpenTransaction(newDepth);
                ancestor.addCloseCallback((context, result) -> onFabricClose(hookedDepth, result.wasCommitted()));
                hooked.set(newDepth);
            }
        }
        return bcStack.get(depth);
    }

    private void onFabricClose(int depth, boolean committed) {
        hooked.clear(depth);

        if (bcStack.size() != depth + 1) {

            while (bcStack.size() > depth) {
                try {
                    bcStack.remove(bcStack.size() - 1).close();
                } catch (RuntimeException ignored) {

                }
            }
            return;
        }

        Transaction bcTx = bcStack.remove(bcStack.size() - 1);
        if (committed) {
            bcTx.commit();
        } else {
            bcTx.close();
        }
    }
}

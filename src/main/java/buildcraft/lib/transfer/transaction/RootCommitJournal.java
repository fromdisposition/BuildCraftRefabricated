/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package buildcraft.lib.transfer.transaction;

import org.jspecify.annotations.Nullable;

public final class RootCommitJournal extends SnapshotJournal<@Nullable Void> {
    private final Runnable rootCommitCallback;

    public RootCommitJournal(Runnable rootCommitCallback) {
        this.rootCommitCallback = rootCommitCallback;
    }

    @Override
    protected @Nullable Void createSnapshot() {
        return null;
    }

    @Override
    protected void revertToSnapshot(@Nullable Void snapshot) {}

    @Override
    protected void onRootCommit(@Nullable Void originalState) {
        rootCommitCallback.run();
    }
}

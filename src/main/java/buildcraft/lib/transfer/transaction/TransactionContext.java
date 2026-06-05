/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package buildcraft.lib.transfer.transaction;

public sealed interface TransactionContext permits Transaction {

    int depth();
}

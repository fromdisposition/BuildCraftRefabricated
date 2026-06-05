/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.lib.debug;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;

public enum BCAdvDebugging {
    INSTANCE;

    @Nullable
    private BlockPos clientTarget = null;

    public void setClientTarget(BlockPos pos) {
        clientTarget = pos == null ? null : pos.immutable();
    }

    @Nullable
    public BlockPos getClientTarget() {
        return clientTarget;
    }

    public void clear() {
        clientTarget = null;
    }
}

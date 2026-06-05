/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package buildcraft.lib.transfer.resource;

import buildcraft.lib.transfer.ResourceHandlerUtil;

public record ResourceStack<T extends Resource>(T resource, int amount) {

    public boolean isEmpty() {
        return ResourceHandlerUtil.isEmpty(resource(), amount());
    }

    @Override
    public String toString() {
        return amount + "x " + resource;
    }
}

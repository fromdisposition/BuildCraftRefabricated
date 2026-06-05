/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package buildcraft.lib.transfer;

import buildcraft.lib.transfer.resource.Resource;

@FunctionalInterface
public interface IndexModifier<T extends Resource> {

    void set(int index, T resource, int amount);
}

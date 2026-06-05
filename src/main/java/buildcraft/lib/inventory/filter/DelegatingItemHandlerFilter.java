/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.inventory.filter;

import net.minecraft.resources.Identifier;

import javax.annotation.Nonnull;

import net.minecraft.world.item.ItemStack;

import buildcraft.lib.transfer.ResourceHandler;
import buildcraft.lib.transfer.item.ItemResource;

import buildcraft.api.core.IStackFilter;

public class DelegatingItemHandlerFilter implements IStackFilter {
    private final ISingleStackFilter perStackFilter;
    private final ResourceHandler<ItemResource> handler;

    public DelegatingItemHandlerFilter(ISingleStackFilter perStackFilter, ResourceHandler<ItemResource> handler) {
        this.perStackFilter = perStackFilter;
        this.handler = handler;
    }

    @Override
    public boolean matches(@Nonnull ItemStack stack) {
        for (int slot = 0; slot < handler.size(); slot++) {
            ItemResource res = handler.getResource(slot);
            if (!res.isEmpty()) {
                ItemStack slotStack = res.toStack(handler.getAmountAsInt(slot));
                if (perStackFilter.matches(slotStack, stack)) {
                    return true;
                }
            }
        }
        return false;
    }
}

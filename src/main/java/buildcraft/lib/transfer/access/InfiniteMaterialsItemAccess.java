/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package buildcraft.lib.transfer.access;

import net.minecraft.world.entity.player.Player;
import buildcraft.lib.transfer.ResourceHandler;
import buildcraft.lib.transfer.ResourceHandlerUtil;
import buildcraft.lib.transfer.TransferPreconditions;
import buildcraft.lib.transfer.item.ItemResource;
import buildcraft.lib.transfer.item.PlayerInventoryWrapper;
import buildcraft.lib.transfer.transaction.TransactionContext;

class InfiniteMaterialsItemAccess implements ItemAccess {
    private final ResourceHandler<ItemResource> mainSlots;
    private final ItemResource resource;
    private final int amount;

    InfiniteMaterialsItemAccess(Player player, ItemResource resource, int amount) {
        this.mainSlots = PlayerInventoryWrapper.of(player).getMainSlots();
        this.resource = resource;
        this.amount = amount;
    }

    @Override
    public ItemResource getResource() {
        return resource;
    }

    @Override
    public int getAmount() {
        return amount;
    }

    @Override
    public int insert(ItemResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);

        if (amount > 0 && !ResourceHandlerUtil.contains(mainSlots, resource)) {

            mainSlots.insert(resource, 1, transaction);
        }

        return amount;
    }

    @Override
    public int extract(ItemResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);

        return amount;
    }
}

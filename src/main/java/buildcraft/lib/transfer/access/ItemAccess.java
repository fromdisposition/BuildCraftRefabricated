/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package buildcraft.lib.transfer.access;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import buildcraft.lib.attachments.ItemAttachment;
import buildcraft.lib.transfer.item.CarriedSlotWrapper;
import buildcraft.lib.transfer.item.PlayerInventoryWrapper;
import buildcraft.lib.transfer.RangedResourceHandler;
import buildcraft.lib.transfer.ResourceHandler;
import buildcraft.lib.transfer.TransferPreconditions;
import buildcraft.lib.transfer.item.ItemResource;
import buildcraft.lib.transfer.transaction.Transaction;
import buildcraft.lib.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

public interface ItemAccess {
    static ItemAccess forPlayerInteraction(Player player, InteractionHand hand) {
        if (player.hasInfiniteMaterials()) {
            return forInfiniteMaterials(player, player.getItemInHand(hand));
        }
        return forPlayerSlot(player, switch (hand) {
            case MAIN_HAND -> player.getInventory().getSelectedSlot();
            case OFF_HAND -> Inventory.SLOT_OFFHAND;
        });
    }

    static ItemAccess forInfiniteMaterials(Player player, ItemStack contents) {
        if (!player.hasInfiniteMaterials()) {
            throw new IllegalArgumentException("Player " + player + " does not have infinite materials");
        }
        return new InfiniteMaterialsItemAccess(player, ItemResource.of(contents), contents.getCount());
    }

    static ItemAccess forPlayerCursor(Player player, AbstractContainerMenu menu) {
        return new PlayerItemAccess(PlayerInventoryWrapper.of(player), CarriedSlotWrapper.of(menu));
    }

    static ItemAccess forPlayerSlot(Player player, int slot) {
        var inventoryWrapper = PlayerInventoryWrapper.of(player);
        return new PlayerItemAccess(inventoryWrapper, RangedResourceHandler.ofSingleIndex(inventoryWrapper, slot));
    }

    static ItemAccess forStack(ItemStack stack) {
        if (stack.isEmpty()) {
            throw new IllegalArgumentException("Expected stack to be non-empty.");
        }
        return new StackItemAccess(stack);
    }

    static ItemAccess forHandlerIndex(ResourceHandler<ItemResource> handler, int index) {
        return new HandlerItemAccess(handler, index);
    }

    static ItemAccess forHandlerIndexStrict(ResourceHandler<ItemResource> handler, int index) {
        return new HandlerItemAccess(RangedResourceHandler.ofSingleIndex(handler, index), 0);
    }

    @ApiStatus.NonExtendable
    default ItemAccess oneByOne() {
        return new OneByOneItemAccess(this);
    }

    @Nullable
    @ApiStatus.NonExtendable
    default <T> T getCapability(ItemAttachment<T, ItemAccess> capability) {
        return capability.getCapability(getResource().toStack(), this);
    }

    ItemResource getResource();

    int getAmount();

    int insert(ItemResource resource, int amount, TransactionContext transaction);

    int extract(ItemResource resource, int amount, TransactionContext transaction);

    @ApiStatus.NonExtendable
    default int exchange(ItemResource newResource, int amount, @Nullable TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(newResource, amount);
        var currentResource = getResource();
        TransferPreconditions.checkNonEmpty(currentResource);

        try (Transaction subTransaction = Transaction.open(transaction)) {
            int extracted = extract(currentResource, amount, subTransaction);
            if (extracted > 0) {
                var inserted = insert(newResource, extracted, subTransaction);
                if (inserted == extracted) {
                    subTransaction.commit();
                    return extracted;
                }
            }
        }

        return 0;
    }
}

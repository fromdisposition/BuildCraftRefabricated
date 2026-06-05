/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package buildcraft.lib.transfer.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import buildcraft.lib.transfer.IndexModifier;
import buildcraft.lib.transfer.ResourceHandler;
import buildcraft.lib.transfer.StacksResourceHandler;
import buildcraft.lib.transfer.transaction.Transaction;
import buildcraft.lib.common.inventory.StackCopySlot;

public class ResourceHandlerSlot extends StackCopySlot {
    private final ResourceHandler<ItemResource> handler;
    private final IndexModifier<ItemResource> slotModifier;

    public ResourceHandlerSlot(ResourceHandler<ItemResource> handler, IndexModifier<ItemResource> slotModifier, int handlerSlot, int xPosition, int yPosition) {
        super(handlerSlot, xPosition, yPosition);
        this.handler = handler;
        this.slotModifier = slotModifier;
    }

    @Override
    public ItemStack getItem() {
        return handler.getResource(this.index).toStack(handler.getAmountAsInt(this.index));
    }

    @Override
    public void set(ItemStack stack) {
        slotModifier.set(this.index, ItemResource.of(stack), stack.getCount());
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return handler.isValid(this.index, ItemResource.of(stack));
    }

    @Override
    public void onQuickCraft(ItemStack oldStackIn, ItemStack newStackIn) {}

    @Override
    public int getMaxStackSize() {
        return handler.getCapacityAsInt(this.index, ItemResource.EMPTY);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return handler.getCapacityAsInt(this.index, ItemResource.of(stack));
    }

    @Override
    public boolean mayPickup(Player player) {
        var resource = handler.getResource(this.index);
        if (resource.isEmpty()) {
            return false;
        }
        try (var tx = Transaction.openRoot()) {
            return handler.extract(this.index, resource, 1, tx) == 1;
        }
    }

    public ResourceHandler<ItemResource> getResourceHandler() {
        return handler;
    }
}

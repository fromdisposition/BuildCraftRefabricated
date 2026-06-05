/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.list;

import javax.annotation.Nonnull;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import buildcraft.fabric.network.BCPayloadContext;

import buildcraft.api.lists.ListMatchHandler;

import buildcraft.lib.gui.ContainerBC_Neptune;
import buildcraft.lib.gui.slot.SlotPhantom;
import buildcraft.lib.list.ListHandler;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.item.ItemHandlerSimple;

import buildcraft.lib.misc.AdvancementUtil;

import buildcraft.core.BCCoreItems;
import buildcraft.core.BCCoreMenuTypes;
import buildcraft.core.PaperAdvancement;
import buildcraft.core.item.ItemList_BC8;

@SuppressWarnings("this-escape")
public class ContainerList extends ContainerBC_Neptune {

    private static final int NET_LABEL = 1;
    private static final int NET_BUTTON = 2;

    public ListHandler.Line[] lines;
    private final InteractionHand hand;

    private final ItemHandlerSimple[] lineInventories;

    public static ContainerList fromNetwork(int containerId, Inventory playerInv, RegistryFriendlyByteBuf buf) {
        InteractionHand hand = InteractionHand.values()[buf.readByte()];
        return new ContainerList(containerId, playerInv, hand);
    }

    public ContainerList(int containerId, Inventory playerInv, InteractionHand hand) {
        super(BCCoreMenuTypes.LIST, containerId, playerInv.player);
        this.hand = hand;

        lines = ListHandler.getLines(getListItemStack());

        lineInventories = new ItemHandlerSimple[lines.length];
        for (int line = 0; line < lines.length; line++) {
            lineInventories[line] = new ItemHandlerSimple(ListHandler.WIDTH);

            for (int slot = 0; slot < ListHandler.WIDTH; slot++) {
                lineInventories[line].setStackInSlot(slot, lines[line].getStack(slot));
            }

            for (int slot = 0; slot < ListHandler.WIDTH; slot++) {
                addSlot(new ListPhantomSlot(lineInventories[line], slot,
                        8 + slot * 18, 32 + line * 34, line));
            }
        }

        addFullPlayerInventory(8, 103);
    }

    private class ListPhantomSlot extends SlotPhantom {
        final int lineIndex;

        ListPhantomSlot(ItemHandlerSimple handler, int slotIndex, int x, int y, int lineIndex) {
            super(handler, slotIndex, x, y, false);
            this.lineIndex = lineIndex;
        }

        @Override
        public void set(@Nonnull ItemStack stack) {
            int slotIndex = handlerIndex;

            if (slotIndex > 0 && lines[lineIndex].isOneStackMode()) {
                return;
            }
            super.set(stack);
            lines[lineIndex].setStack(slotIndex, stack);
            ItemStack listStack = getListItemStack();
            ListHandler.saveLines(listStack, lines);
            ItemList_BC8.updateModelData(listStack);
            AdvancementUtil.unlockAdvancement(player, PaperAdvancement.ID, PaperAdvancement.WRITE_TO_LIST);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return !getListItemStack().isEmpty();
    }

    @Nonnull
    public ItemStack getListItemStack() {
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.isEmpty() && stack.getItem() instanceof ItemList_BC8) {
            return stack;
        }
        return ItemStack.EMPTY;
    }

    public void switchButton(final int lineIndex, final int button) {
        lines[lineIndex].toggleOption(button);

        if (player.level().isClientSide()) {
            sendMessage(NET_BUTTON, (buffer) -> {
                buffer.writeByte(lineIndex);
                buffer.writeByte(button);
            });
        }

        if (button == 1 || button == 2) {
            if (lines[lineIndex].isOneStackMode()) {
                for (int i = 1; i < ListHandler.WIDTH; i++) {
                    lineInventories[lineIndex].setStackInSlot(i, ItemStack.EMPTY);
                    lines[lineIndex].setStack(i, ItemStack.EMPTY);
                }
            }
        }

        ItemStack listStack = getListItemStack();
        ListHandler.saveLines(listStack, lines);
        ItemList_BC8.updateModelData(listStack);
    }

    public void setLabel(final String text) {
        ItemStack stack = getListItemStack();
        if (!stack.isEmpty() && stack.getItem() instanceof ItemList_BC8 list) {
            list.setLocationName(stack, text);
        }

        if (player.level().isClientSide()) {
            sendMessage(NET_LABEL, (buffer) -> buffer.writeUtf(text));
        }
    }

    @Override
    public void readMessage(int id, PacketBufferBC buffer, boolean isClient, BCPayloadContext ctx) {
        super.readMessage(id, buffer, isClient, ctx);
        if (!isClient) {
            if (id == NET_BUTTON) {
                int lineIndex = buffer.readUnsignedByte();
                int button = buffer.readUnsignedByte();
                if (lineIndex >= 0 && lineIndex < lines.length && button >= 0 && button < 3) {
                    switchButton(lineIndex, button);
                }
            } else if (id == NET_LABEL) {
                setLabel(buffer.readUtf(1024));
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        return ItemStack.EMPTY;
    }
}

/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.builders.container;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import buildcraft.fabric.network.BCPayloadContext;

import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.slot.SlotBase;
import buildcraft.lib.net.PacketBufferBC;

import buildcraft.builders.BCBuildersMenuTypes;
import buildcraft.builders.item.ItemSnapshot;
import buildcraft.builders.snapshot.Snapshot;
import buildcraft.builders.tile.TileReplacer;

@SuppressWarnings("this-escape")
public class ContainerReplacer extends ContainerBCTile<TileReplacer> {

    public static final int NET_REPLACE = 10;

    public ContainerReplacer(int containerId, Inventory playerInv, BlockPos pos) {
        this(containerId, playerInv, getTile(playerInv, pos));
    }

    public ContainerReplacer(int containerId, Inventory playerInv, TileReplacer tile) {
        super(BCBuildersMenuTypes.REPLACER, containerId, playerInv.player, tile);

        if (tile != null) {
            addSlot(new SlotBase(tile.invSnapshot, 0, 8, 115));
            addSlot(new SlotBase(tile.invSchematicFrom, 0, 8, 137));
            addSlot(new SlotBase(tile.invSchematicTo, 0, 56, 137));
        }

        addFullPlayerInventory(8, 159, playerInv);
    }

    private static TileReplacer getTile(Inventory playerInv, BlockPos pos) {
        if (playerInv.player.level() != null) {
            var be = playerInv.player.level().getBlockEntity(pos);
            if (be instanceof TileReplacer replacer) {
                return replacer;
            }
        }
        return null;
    }

    public String getBlueprintName() {
        if (this.slots.isEmpty()) {
            return "";
        }
        ItemStack stack = getSlot(0).getItem();
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemSnapshot)) {
            return "";
        }
        Snapshot.Header header = ItemSnapshot.getHeader(stack);
        if (header == null || header.name == null) {
            return "";
        }
        return header.name;
    }

    @Override
    public void readMessage(int id, PacketBufferBC buffer, boolean isClient, BCPayloadContext ctx) {
        if (id == NET_REPLACE && !isClient) {
            String newName = buffer.readUtf();
            if (tile != null) {
                tile.doReplace(newName);
            }
            return;
        }
        super.readMessage(id, buffer, isClient, ctx);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {

        return ItemStack.EMPTY;
    }
}

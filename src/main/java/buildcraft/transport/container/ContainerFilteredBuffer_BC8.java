/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.transport.container;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.slot.SlotBase;
import buildcraft.lib.gui.slot.SlotPhantom;
import buildcraft.transport.BCTransportMenuTypes;
import buildcraft.transport.tile.TileFilteredBuffer;

@SuppressWarnings("this-escape")
public class ContainerFilteredBuffer_BC8 extends ContainerBCTile<TileFilteredBuffer> {

    public ContainerFilteredBuffer_BC8(int containerId, Inventory playerInv, BlockPos pos) {
        this(containerId, playerInv, getTile(playerInv, pos));
    }

    public ContainerFilteredBuffer_BC8(int containerId, Inventory playerInv, TileFilteredBuffer tile) {
        super(BCTransportMenuTypes.FILTERED_BUFFER, containerId, playerInv.player, tile);

        for (int i = 0; i < 9; i++) {

            addSlot(new SlotPhantom(tile.invFilter, i, 8 + i * 18, 27, false));

            SlotBase mainSlot = new SlotBase(tile.invMain, i, 8 + i * 18, 61);
            addSlot(mainSlot);
        }

        addFullPlayerInventory(8, 86);
    }

    private static TileFilteredBuffer getTile(Inventory playerInv, BlockPos pos) {
        if (playerInv.player.level() != null) {
            var be = playerInv.player.level().getBlockEntity(pos);
            if (be instanceof TileFilteredBuffer filtered) {
                return filtered;
            }
        }
        return null;
    }

}

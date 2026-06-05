/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.container;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.slot.SlotBase;
import buildcraft.lib.gui.slot.SlotDisplay;
import buildcraft.lib.gui.slot.SlotOutput;

import buildcraft.silicon.BCSiliconMenuTypes;
import buildcraft.silicon.tile.TileIntegrationTable;

@SuppressWarnings("this-escape")
public class ContainerIntegrationTable extends ContainerBCTile<TileIntegrationTable> {

    public ContainerIntegrationTable(int containerId, Inventory playerInv, BlockPos pos) {
        this(containerId, playerInv.player, getTile(playerInv, pos));
    }

    public ContainerIntegrationTable(int containerId, Player player, TileIntegrationTable tile) {
        super(BCSiliconMenuTypes.INTEGRATION_TABLE, containerId, player, tile);

        int[] indexes = {0, 1, 2, 3, 0, 4, 5, 6, 7};

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                addSlot(new SlotBase(
                    (x == 1 && y == 1) ? tile.invTarget : tile.invToIntegrate,
                    indexes[x + y * 3],
                    19 + x * 25, 24 + y * 25
                ));
            }
        }

        addSlot(new SlotDisplay(i -> tile.getOutput(), 0, 101, 36));

        addSlot(new SlotOutput(tile.invResult, 0, 138, 49));

        addFullPlayerInventory(8, 109);
    }

    private static TileIntegrationTable getTile(Inventory inv, BlockPos pos) {
        var be = inv.player.level().getBlockEntity(pos);
        return be instanceof TileIntegrationTable t ? t : null;
    }
}

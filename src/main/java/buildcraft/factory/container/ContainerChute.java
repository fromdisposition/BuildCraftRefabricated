/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.container;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;

import buildcraft.factory.BCFactoryMenuTypes;
import buildcraft.lib.fabric.menu.MenuBlockEntityLookup;
import buildcraft.factory.tile.TileChute;
import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.slot.SlotBase;

@SuppressWarnings("this-escape")
public class ContainerChute extends ContainerBCTile<TileChute> {

    public ContainerChute(int containerId, Inventory playerInv, BlockPos pos) {
        this(containerId, playerInv, MenuBlockEntityLookup.get(playerInv, pos, TileChute.class));
    }

    public ContainerChute(int containerId, Inventory playerInv, TileChute tile) {
        super(BCFactoryMenuTypes.CHUTE, containerId, playerInv.player, tile);

        if (tile != null) {

            addSlot(new SlotBase(tile.inv, 0, 62, 18));
            addSlot(new SlotBase(tile.inv, 1, 80, 18));
            addSlot(new SlotBase(tile.inv, 2, 98, 18));
            addSlot(new SlotBase(tile.inv, 3, 80, 36));
        }

        addFullPlayerInventory(8, 71);
    }

}

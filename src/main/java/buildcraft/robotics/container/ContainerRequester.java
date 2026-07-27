/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.container;

import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.slot.SlotBase;
import buildcraft.lib.gui.slot.SlotPhantom;
import buildcraft.robotics.BCRoboticsMenuTypes;
import buildcraft.robotics.tile.TileRequester;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;

public class ContainerRequester extends ContainerBCTile<TileRequester> {
   public ContainerRequester(int containerId, Inventory playerInv, BlockPos pos) {
      this(containerId, playerInv, getTile(playerInv, pos));
   }

   public ContainerRequester(int containerId, Inventory playerInv, TileRequester tile) {
      super(BCRoboticsMenuTypes.REQUESTER, containerId, playerInv.player, tile);
      if (tile != null) {
         for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 5; y++) {
               int index = x * 5 + y;
               this.addSlot(new SlotPhantom(tile.invRequests, index, 9 + x * 18, 18 + y * 18, true));
               this.addSlot(new SlotBase(tile.invItems, index, 117 + x * 18, 18 + y * 18));
            }
         }
      }

      this.addFullPlayerInventory(19, 122);
   }

   private static TileRequester getTile(Inventory inv, BlockPos pos) {
      return inv.player.level().getBlockEntity(pos) instanceof TileRequester requester ? requester : null;
   }
}

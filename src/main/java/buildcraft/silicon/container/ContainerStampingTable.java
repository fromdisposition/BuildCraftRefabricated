/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.container;

import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.slot.SlotOutput;
import buildcraft.lib.gui.slot.SlotBase;
import buildcraft.silicon.BCSiliconMenuTypes;
import buildcraft.silicon.tile.TileStampingTable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class ContainerStampingTable extends ContainerBCTile<TileStampingTable> {
   public ContainerStampingTable(int containerId, Inventory playerInv, BlockPos pos) {
      this(containerId, playerInv.player, getTile(playerInv, pos));
   }

   public ContainerStampingTable(int containerId, Player player, TileStampingTable tile) {
      super(BCSiliconMenuTypes.STAMPING_TABLE, containerId, player, tile);
      if (tile != null) {
         this.addSlot(new SlotBase(tile.invInput, 0, 14, 22));
         this.addSlot(new SlotOutput(tile.invOutput, 0, 142, 22));
         this.addSlot(new SlotOutput(tile.invOutput, 1, 110, 48));
         this.addSlot(new SlotOutput(tile.invOutput, 2, 128, 48));
         this.addSlot(new SlotOutput(tile.invOutput, 3, 146, 48));
      }

      this.addFullPlayerInventory(8, 80);
   }

   private static TileStampingTable getTile(Inventory inv, BlockPos pos) {
      return inv.player.level().getBlockEntity(pos) instanceof TileStampingTable table ? table : null;
   }
}

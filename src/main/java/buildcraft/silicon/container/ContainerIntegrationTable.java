/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.container;

import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.slot.SlotBase;
import buildcraft.lib.gui.slot.SlotDisplay;
import buildcraft.lib.gui.slot.SlotOutput;
import buildcraft.silicon.BCSiliconMenuTypes;
import buildcraft.silicon.tile.TileIntegrationTable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class ContainerIntegrationTable extends ContainerBCTile<TileIntegrationTable> {
   public ContainerIntegrationTable(int containerId, Inventory playerInv, BlockPos pos) {
      this(containerId, playerInv.player, getTile(playerInv, pos));
   }

   public ContainerIntegrationTable(int containerId, Player player, TileIntegrationTable tile) {
      super(BCSiliconMenuTypes.INTEGRATION_TABLE, containerId, player, tile);
      if (tile == null) {
         this.addFullPlayerInventory(8, 104);
      } else {
         int[] indexes = new int[]{0, 1, 2, 3, 0, 4, 5, 6, 7};

         for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
               this.addSlot(new SlotBase(x == 1 && y == 1 ? tile.invTarget : tile.invToIntegrate, indexes[x + y * 3], 9 + x * 25, 20 + y * 25));
            }
         }

         this.addSlot(new SlotDisplay(i -> tile.getOutput(), 0, 94, 27));
         this.addSlot(new SlotOutput(tile.invResult, 0, 133, 45));
         this.addFullPlayerInventory(8, 104);
      }
   }

   private static TileIntegrationTable getTile(Inventory inv, BlockPos pos) {
      return inv.player.level().getBlockEntity(pos) instanceof TileIntegrationTable t ? t : null;
   }
}

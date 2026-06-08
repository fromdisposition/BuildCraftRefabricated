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
import buildcraft.silicon.tile.TileProgrammingTable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class ContainerProgrammingTable extends ContainerBCTile<TileProgrammingTable> {
   public ContainerProgrammingTable(int containerId, Inventory playerInv, BlockPos pos) {
      this(containerId, playerInv.player, getTile(playerInv, pos));
   }

   public ContainerProgrammingTable(int containerId, Player player, TileProgrammingTable tile) {
      super(BCSiliconMenuTypes.PROGRAMMING_TABLE, containerId, player, tile);
      if (tile != null) {
         this.addSlot(new SlotBase(tile.invInput, 0, 8, 36) {
            @Override
            public void setChanged() {
               super.setChanged();
               tile.onInputChanged();
            }
         });
         this.addSlot(new SlotOutput(tile.invOutput, 0, 8, 90));

         for (int y = 0; y < TileProgrammingTable.HEIGHT; y++) {
            for (int x = 0; x < TileProgrammingTable.WIDTH; x++) {
               int index = y * TileProgrammingTable.WIDTH + x;
               this.addSlot(new SlotDisplay(i -> tile.getOptionStack(index), index, 43 + x * 18, 36 + y * 18));
            }
         }
      }

      this.addFullPlayerInventory(8, 126);
   }

   public boolean clickMenuButton(Player player, int index) {
      if (this.tile == null) {
         return false;
      }

      this.tile.selectOption(index);
      return true;
   }

   private static TileProgrammingTable getTile(Inventory inv, BlockPos pos) {
      return inv.player.level().getBlockEntity(pos) instanceof TileProgrammingTable table ? table : null;
   }
}

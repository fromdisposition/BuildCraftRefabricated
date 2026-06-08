/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.container;

import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.slot.SlotBase;
import buildcraft.silicon.BCSiliconMenuTypes;
import buildcraft.silicon.tile.TileChargingTable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ContainerChargingTable extends ContainerBCTile<TileChargingTable> {
   public ContainerChargingTable(int containerId, Inventory playerInv, BlockPos pos) {
      this(containerId, playerInv.player, getTile(playerInv, pos));
   }

   public ContainerChargingTable(int containerId, Player player, TileChargingTable tile) {
      super(BCSiliconMenuTypes.CHARGING_TABLE, containerId, player, tile);
      if (tile != null) {
         this.addSlot(new SlotBase(tile.inv, 0, 80, 18) {
            @Override
            public boolean mayPlace(ItemStack stack) {
               return tile.isValidChargeItem(stack);
            }
         });
      }

      this.addFullPlayerInventory(8, 51);
   }

   private static TileChargingTable getTile(Inventory inv, BlockPos pos) {
      return inv.player.level().getBlockEntity(pos) instanceof TileChargingTable table ? table : null;
   }
}

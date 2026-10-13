/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.container;

import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.slot.SlotBase;
import buildcraft.lib.gui.slot.SlotPhantom;
import buildcraft.transport.BCTransportMenuTypes;
import buildcraft.transport.tile.TileFilteredBuffer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;

public class ContainerFilteredBuffer_BC8 extends ContainerBCTile<TileFilteredBuffer> {
   public ContainerFilteredBuffer_BC8(int containerId, Inventory playerInv, BlockPos pos) {
      this(containerId, playerInv, getTile(playerInv, pos));
   }

   public ContainerFilteredBuffer_BC8(int containerId, Inventory playerInv, TileFilteredBuffer tile) {
      super(BCTransportMenuTypes.FILTERED_BUFFER, containerId, playerInv.player, tile);

      for (int i = 0; i < 9; i++) {
         this.addSlot(new SlotPhantom(tile.invFilter, i, 8 + i * 18, 18, false));
         SlotBase mainSlot = new SlotBase(tile.invMain, i, 8 + i * 18, 52);
         this.addSlot(mainSlot);
      }

      this.addFullPlayerInventory(8, 84);
   }

   private static TileFilteredBuffer getTile(Inventory playerInv, BlockPos pos) {
      return playerInv.player.level() != null && playerInv.player.level().getBlockEntity(pos) instanceof TileFilteredBuffer filtered ? filtered : null;
   }
}

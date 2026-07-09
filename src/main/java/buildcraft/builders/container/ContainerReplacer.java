/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.container;

import net.minecraft.network.FriendlyByteBuf;
import buildcraft.builders.BCBuildersMenuTypes;
import buildcraft.builders.item.ItemSnapshot;
import buildcraft.builders.snapshot.Snapshot;
import buildcraft.builders.tile.TileReplacer;
import buildcraft.fabric.network.BCPayloadContext;
import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.slot.SlotBase;
import buildcraft.lib.net.PacketBufferBC;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class ContainerReplacer extends ContainerBCTile<TileReplacer> {
   public static final int NET_REPLACE = 10;

   public ContainerReplacer(int containerId, Inventory playerInv, BlockPos pos) {
      this(containerId, playerInv, getTile(playerInv, pos));
   }

   public ContainerReplacer(int containerId, Inventory playerInv, TileReplacer tile) {
      super(BCBuildersMenuTypes.REPLACER, containerId, playerInv.player, tile);
      if (tile != null) {
         this.addSlot(new SlotBase(tile.invSnapshot, 0, 8, 120));
         this.addSlot(new SlotBase(tile.invSchematicFrom, 0, 8, 142));
         this.addSlot(new SlotBase(tile.invSchematicTo, 0, 56, 142));
      }

      this.addFullPlayerInventory(8, 174, playerInv);
   }

   private static TileReplacer getTile(Inventory playerInv, BlockPos pos) {
      return playerInv.player.level() != null && playerInv.player.level().getBlockEntity(pos) instanceof TileReplacer replacer ? replacer : null;
   }

   public String getBlueprintName() {
      if (this.slots.isEmpty()) {
         return "";
      } else {
         ItemStack stack = this.getSlot(0).getItem();
         if (!stack.isEmpty() && stack.getItem() instanceof ItemSnapshot) {
            Snapshot.Header header = ItemSnapshot.getHeader(stack);
            return header != null && header.name != null ? header.name : "";
         } else {
            return "";
         }
      }
   }

   @Override
   public void readMessage(int id, FriendlyByteBuf buffer, boolean isClient, BCPayloadContext ctx) {
      if (id == 10 && !isClient) {
         String newName = buffer.readUtf();
         if (this.tile != null) {
            this.tile.doReplace(newName);
         }
      } else {
         super.readMessage(id, buffer, isClient, ctx);
      }
   }
}

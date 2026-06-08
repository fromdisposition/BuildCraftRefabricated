/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.container;

import net.minecraft.network.FriendlyByteBuf;
import buildcraft.builders.BCBuildersMenuTypes;
import buildcraft.builders.item.ItemSnapshot;
import buildcraft.builders.tile.TileArchitectTable;
import buildcraft.fabric.network.BCPayloadContext;
import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.net.PacketBufferBC;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerArchitectTable extends ContainerBCTile<TileArchitectTable> {
   public static final int NET_SET_NAME = 10;
   private static final int DATA_SCANNING = 0;
   private static final int DATA_PROGRESS = 1;
   private static final int DATA_TOTAL = 2;
   private static final int DATA_VALID = 3;
   private static final int DATA_COUNT = 4;
   private final ContainerData data;
   private final ContainerArchitectTable.SnapshotContainer snapshotContainer;

   public ContainerArchitectTable(int containerId, Inventory playerInv, BlockPos pos) {
      this(containerId, playerInv, getTile(playerInv, pos));
   }

   public ContainerArchitectTable(int containerId, Inventory playerInv, final TileArchitectTable tile) {
      super(BCBuildersMenuTypes.ARCHITECT, containerId, playerInv.player, tile);
      if (tile != null && tile.getLevel() != null && !tile.getLevel().isClientSide()) {
         this.data = new ContainerData() {
            public int get(int index) {
               return switch (index) {
                  case 0 -> tile.isScanning() ? 1 : 0;
                  case 1 -> tile.getScanProgress();
                  case 2 -> tile.getScanTotal();
                  case 3 -> tile.getIsValid() ? 1 : 0;
                  default -> 0;
               };
            }

            public void set(int index, int value) {
            }

            public int getCount() {
               return 4;
            }
         };
      } else {
         this.data = new SimpleContainerData(4);
      }

      this.addDataSlots(this.data);
      this.snapshotContainer = new ContainerArchitectTable.SnapshotContainer(tile);
      this.addSlot(new ContainerArchitectTable.SnapshotInputSlot(this.snapshotContainer, 0, 52, 125));
      this.addSlot(new ContainerArchitectTable.SnapshotOutputSlot(this.snapshotContainer, 1, 111, 125));
      this.addFullPlayerInventory(8, 158, playerInv);
   }

   private static TileArchitectTable getTile(Inventory playerInv, BlockPos pos) {
      return playerInv.player.level() != null && playerInv.player.level().getBlockEntity(pos) instanceof TileArchitectTable architect ? architect : null;
   }

   public boolean getSyncedScanning() {
      return this.data.get(0) != 0;
   }

   public int getSyncedProgress() {
      return this.data.get(1);
   }

   public int getSyncedTotal() {
      return this.data.get(2);
   }

   public boolean getSyncedValid() {
      return this.data.get(3) != 0;
   }

   public String getTileName() {
      return this.tile != null ? this.tile.name : "<unnamed>";
   }

   public void setTileName(String newName) {
      if (this.tile != null && this.tile.getLevel() != null) {
         this.tile.name = newName;
         if (!this.tile.getLevel().isClientSide()) {
            this.tile.setChanged();
         }
      }
   }

   @Override
   public void readMessage(int id, FriendlyByteBuf buffer, boolean isClient, BCPayloadContext ctx) {
      if (id == 10 && !isClient) {
         String newName = buffer.readUtf();
         this.setTileName(newName);
      } else {
         super.readMessage(id, buffer, isClient, ctx);
      }
   }

   private static class SnapshotContainer implements Container {
      private final TileArchitectTable tile;

      SnapshotContainer(TileArchitectTable tile) {
         this.tile = tile;
      }

      public int getContainerSize() {
         return 2;
      }

      public boolean isEmpty() {
         return this.tile == null || this.tile.getSnapshotIn().isEmpty() && this.tile.getSnapshotOut().isEmpty();
      }

      public ItemStack getItem(int slot) {
         if (this.tile == null) {
            return ItemStack.EMPTY;
         } else {
            return slot == 0 ? this.tile.getSnapshotIn() : this.tile.getSnapshotOut();
         }
      }

      public ItemStack removeItem(int slot, int count) {
         if (this.tile == null) {
            return ItemStack.EMPTY;
         }

         ItemStack current = this.getItem(slot);
         if (current.isEmpty()) {
            return ItemStack.EMPTY;
         }

         ItemStack result = current.split(count);
         if (current.isEmpty()) {
            this.setItem(slot, ItemStack.EMPTY);
         } else {
            this.setChanged();
         }

         return result;
      }

      public ItemStack removeItemNoUpdate(int slot) {
         if (this.tile == null) {
            return ItemStack.EMPTY;
         }

         ItemStack current = this.getItem(slot);
         this.setItem(slot, ItemStack.EMPTY);
         return current;
      }

      public void setItem(int slot, ItemStack stack) {
         if (this.tile != null) {
            if (slot == 0) {
               this.tile.setSnapshotIn(stack);
            } else {
               this.tile.setSnapshotOut(stack);
            }
         }
      }

      public void setChanged() {
         if (this.tile != null) {
            this.tile.setChanged();
         }
      }

      public boolean stillValid(Player player) {
         return true;
      }

      public void clearContent() {
         if (this.tile != null) {
            this.tile.setSnapshotIn(ItemStack.EMPTY);
            this.tile.setSnapshotOut(ItemStack.EMPTY);
         }
      }
   }

   private static class SnapshotInputSlot extends Slot {
      public SnapshotInputSlot(Container container, int index, int x, int y) {
         super(container, index, x, y);
      }

      public boolean mayPlace(ItemStack stack) {
         return stack.getItem() instanceof ItemSnapshot;
      }
   }

   private static class SnapshotOutputSlot extends Slot {
      public SnapshotOutputSlot(Container container, int index, int x, int y) {
         super(container, index, x, y);
      }

      public boolean mayPlace(ItemStack stack) {
         return false;
      }
   }
}

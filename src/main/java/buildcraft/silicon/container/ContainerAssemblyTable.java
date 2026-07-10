/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.container;

import net.minecraft.network.FriendlyByteBuf;
import buildcraft.fabric.network.BCPayloadContext;
import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.slot.SlotBase;
import buildcraft.lib.gui.slot.SlotDisplay;
//? if has_jei {
import buildcraft.lib.integration.jei.JeiTransferUtil;
//?}
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.silicon.BCSiliconMenuTypes;
import buildcraft.silicon.EnumAssemblyRecipeState;
import buildcraft.silicon.tile.TileAssemblyTable;
import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ContainerAssemblyTable extends ContainerBCTile<TileAssemblyTable> {
   public ContainerAssemblyTable(int containerId, Inventory playerInv, BlockPos pos) {
      this(containerId, playerInv.player, getTile(playerInv, pos));
   }

   public ContainerAssemblyTable(int containerId, Player player, TileAssemblyTable tile) {
      super(BCSiliconMenuTypes.ASSEMBLY_TABLE, containerId, player, tile);
      // Guard the null tile the same way ContainerIntegrationTable does: the client factory resolves the tile from
      // the world and gets null when the block entity is not yet synced, and dereferencing tile.inv here NPEs the
      // client. Still add the player inventory so the slot count matches the server (else the content packet
      // indexes past an empty slot list and disconnects).
      if (tile == null) {
         this.addFullPlayerInventory(8, 104);
      } else {
         for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 3; x++) {
               this.addSlot(new SlotBase(tile.inv, x + y * 3, 8 + x * 18, 18 + y * 18));
            }
         }

         for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 3; x++) {
               this.addSlot(new SlotDisplay(this::getDisplay, x + y * 3, 116 + x * 18, 18 + y * 18));
            }
         }

         this.addFullPlayerInventory(8, 104);
      }
   }

   public boolean clickMenuButton(Player player, int index) {
      if (this.tile == null) {
         return false;
      }

      ArrayList<TileAssemblyTable.AssemblyInstruction> keys = new ArrayList<>(this.tile.recipesStates.keySet());
      if (index >= 0 && index < keys.size()) {
         TileAssemblyTable.AssemblyInstruction instruction = keys.get(index);
         EnumAssemblyRecipeState current = this.tile.recipesStates.get(instruction);
         if (current == EnumAssemblyRecipeState.POSSIBLE) {
            this.tile.recipesStates.put(instruction, EnumAssemblyRecipeState.SAVED);
         } else if (current == EnumAssemblyRecipeState.SAVED) {
            this.tile.recipesStates.put(instruction, EnumAssemblyRecipeState.POSSIBLE);
         } else if (current == EnumAssemblyRecipeState.PAUSED) {
            this.tile.recipesStates.put(instruction, EnumAssemblyRecipeState.SAVED);
         } else {
            this.tile.recipesStates.put(instruction, EnumAssemblyRecipeState.PAUSED);
         }

         this.tile.setChanged();
         if (this.tile.getLevel() != null) {
            this.tile.getLevel().sendBlockUpdated(this.tile.getBlockPos(), this.tile.getBlockState(), this.tile.getBlockState(), 3);
         }

         return true;
      } else {
         return false;
      }
   }

   @Override
   public void readMessage(int id, FriendlyByteBuf buffer, boolean isClient, BCPayloadContext ctx) {
      if (id == 102 && !isClient && this.tile != null) {
         boolean maxTransfer = buffer.readBoolean();
         int count = buffer.readVarInt();

         for (int i = 0; i < count; i++) {
            CompoundTag tag = buffer.readNbt();
            ItemStack want = tag == null ? ItemStack.EMPTY : NBTUtilBC.itemStackFromNBT(tag);
            if (!want.isEmpty()) {
               int limit = maxTransfer ? Integer.MAX_VALUE : want.getCount();
               //? if has_jei {
               JeiTransferUtil.moveMatchingToHandler(this.player.getInventory(), want, limit, this.tile.inv);
               //?}
            }
         }
      } else {
         super.readMessage(id, buffer, isClient, ctx);
      }
   }

   private ItemStack getDisplay(int index) {
      return index < this.tile.recipesStates.size() ? new ArrayList<>(this.tile.recipesStates.keySet()).get(index).output : ItemStack.EMPTY;
   }

   private static TileAssemblyTable getTile(Inventory inv, BlockPos pos) {
      return inv.player.level().getBlockEntity(pos) instanceof TileAssemblyTable t ? t : null;
   }
}

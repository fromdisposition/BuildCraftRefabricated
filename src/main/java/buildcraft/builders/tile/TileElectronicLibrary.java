/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.tile;

import buildcraft.api.core.BCLog;
import buildcraft.api.core.EnumPipePart;
import buildcraft.api.enums.EnumSnapshotType;
import buildcraft.builders.BCBuildersBlockEntities;
import buildcraft.builders.BCBuildersItems;
import buildcraft.builders.container.ContainerElectronicLibrary;
import buildcraft.builders.item.ItemSnapshot;
import buildcraft.builders.snapshot.GlobalSavedDataSnapshots;
import buildcraft.builders.snapshot.Snapshot;
import buildcraft.lib.fabric.menu.BlockEntityExtendedMenu;
import buildcraft.lib.nbt.NbtSquisher;
import buildcraft.lib.tile.BcBlockEntity;
import buildcraft.lib.tile.ItemHandlerManager;
import buildcraft.lib.tile.ItemHandlerSimple;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TileElectronicLibrary extends BcBlockEntity implements MenuProvider, BlockEntityExtendedMenu {
   public final ItemHandlerSimple invDownIn = this.itemManager
      .addInvHandler(
         "downIn", 1, (slot, stack) -> stack.getItem() instanceof ItemSnapshot snap && snap.isUsed(), ItemHandlerManager.EnumAccess.INSERT, EnumPipePart.VALUES
      );
   public final ItemHandlerSimple invDownOut = this.itemManager.addInvHandler("downOut", 1, ItemHandlerManager.EnumAccess.EXTRACT, EnumPipePart.VALUES);
   public final ItemHandlerSimple invUpIn = this.itemManager
      .addInvHandler("upIn", 1, (slot, stack) -> stack.getItem() instanceof ItemSnapshot, ItemHandlerManager.EnumAccess.INSERT, EnumPipePart.VALUES);
   public final ItemHandlerSimple invUpOut = this.itemManager.addInvHandler("upOut", 1, ItemHandlerManager.EnumAccess.EXTRACT, EnumPipePart.VALUES);
   public Snapshot.@Nullable Key selected = null;
   public int progressDown = -1;
   public int progressUp = -1;
   private final Set<Player> watchingPlayers = new HashSet<>();
   private static final int MIN_TICKS_BETWEEN_DOWNLOADS = 4;
   private long lastDownloadBroadcastTick = -4L;
   private boolean uploadRequestInFlight;

   public TileElectronicLibrary(BlockPos pos, BlockState state) {
      super(BCBuildersBlockEntities.LIBRARY, pos, state);
   }

   @Override
   public void onPlacedBy(@Nullable LivingEntity placer, ItemStack stack) {
      super.onPlacedBy(placer, stack);
      if (this.level != null && !this.level.isClientSide()) {
         this.setChanged();
         this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
      }
   }

   @Override
   public void onPlayerOpen(Player player) {
      super.onPlayerOpen(player);
      this.watchingPlayers.add(player);
   }

   @Override
   public void onPlayerClose(Player player) {
      super.onPlayerClose(player);
      this.watchingPlayers.remove(player);
   }

   public void tick() {
      if (this.level != null && !this.level.isClientSide()) {
         if (!this.invDownIn.getStackInSlot(0).isEmpty() && this.invDownOut.getStackInSlot(0).isEmpty()) {
            if (this.progressDown == -1) {
               this.progressDown = 0;
            }

            if (this.progressDown >= 50) {
               ItemStack inStack = this.invDownIn.getStackInSlot(0);
               this.invDownOut.setStackInSlot(0, inStack.copy());
               this.invDownIn.setStackInSlot(0, ItemStack.EMPTY);
               this.progressDown = -1;
               this.setChanged();
               this.broadcastDownload(inStack);
            } else {
               this.progressDown++;
            }
         } else if (this.progressDown != -1) {
            this.progressDown = -1;
         }

         if (this.selected != null && !this.invUpIn.getStackInSlot(0).isEmpty() && this.invUpOut.getStackInSlot(0).isEmpty()) {
            if (this.progressUp == -1) {
               this.progressUp = 0;
            }

            if (this.progressUp >= 50) {
               this.requestUpload();
               this.progressUp = -1;
            } else {
               this.progressUp++;
            }
         } else if (this.progressUp != -1) {
            this.progressUp = -1;
            this.uploadRequestInFlight = false;
         }
      }
   }

   private void broadcastDownload(ItemStack usedItem) {
      long tick = this.level.getGameTime();
      if (tick - this.lastDownloadBroadcastTick >= 4L) {
         this.lastDownloadBroadcastTick = tick;
         Snapshot.Header header = ItemSnapshot.getHeader(usedItem);
         if (header != null) {
            Snapshot snapshot = GlobalSavedDataSnapshots.get(this.level).getSnapshot(header.key);
            if (snapshot != null) {
               Snapshot snapshotWithHeader = snapshot.copy();
               snapshotWithHeader.key = new Snapshot.Key(snapshot.key, header);

               byte[] data;
               try {
                  ByteArrayOutputStream baos = new ByteArrayOutputStream();
                  NbtSquisher.squishVanilla(Snapshot.writeToNBT(snapshotWithHeader), baos);
                  data = baos.toByteArray();
               } catch (IOException e) {
                  BCLog.logger.warn("[library] Failed to serialize snapshot for download broadcast", e);
                  return;
               }

               for (Player p : this.watchingPlayers) {
                  if (p.containerMenu instanceof ContainerElectronicLibrary container) {
                     container.sendDownloadData(data);
                  }
               }
            }
         }
      }
   }

   private void requestUpload() {
      if (this.selected != null && !this.uploadRequestInFlight) {
         this.uploadRequestInFlight = true;
         Snapshot.Key key = this.selected;

         for (Player p : this.watchingPlayers) {
            if (p.containerMenu instanceof ContainerElectronicLibrary container) {
               container.sendMessage(3, buf -> key.writeToByteBuf(buf));
               break;
            }
         }
      }
   }

   public void onUploadReceived(Snapshot snapshot) {
      this.uploadRequestInFlight = false;
      GlobalSavedDataSnapshots.get(this.level).addSnapshot(snapshot);
      Snapshot.Header header = snapshot.key.header;
      if (header == null) {
         header = new Snapshot.Header(snapshot.key, new UUID(0L, 0L), new Date(), "Snapshot");
      }

      EnumSnapshotType type = snapshot.getType();
      ItemSnapshot usedItem = type == EnumSnapshotType.BLUEPRINT ? BCBuildersItems.BLUEPRINT_USED : BCBuildersItems.TEMPLATE_USED;
      this.invUpOut.setStackInSlot(0, usedItem.createUsedStack(header));
      this.invUpIn.setStackInSlot(0, ItemStack.EMPTY);
      this.setChanged();
   }

   @Override
   protected void saveAdditional(ValueOutput output) {
      super.saveAdditional(output);
      output.putInt("progressDown", this.progressDown);
      output.putInt("progressUp", this.progressUp);
      output.store("items", CompoundTag.CODEC, this.itemManager.serializeNBT());
      if (this.selected != null) {
         output.store("selected", CompoundTag.CODEC, this.selected.serializeNBT());
      }
   }

   @Override
   public void loadAdditional(ValueInput input) {
      super.loadAdditional(input);
      this.progressDown = input.getIntOr("progressDown", -1);
      this.progressUp = input.getIntOr("progressUp", -1);
      input.read("items", CompoundTag.CODEC).ifPresent(this.itemManager::deserializeNBT);
      this.selected = input.read("selected", CompoundTag.CODEC).map(Snapshot.Key::new).orElse(null);
   }

   public Component getDisplayName() {
      return Component.translatable("tile.buildcraftbuilders.library.name");
   }

   @Nullable
   public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
      return new ContainerElectronicLibrary(containerId, playerInv, this);
   }
}

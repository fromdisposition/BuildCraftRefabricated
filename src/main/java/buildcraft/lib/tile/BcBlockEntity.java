/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.tile;

import buildcraft.api.transport.pipe.IPipeHolder;
import com.mojang.authlib.GameProfile;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public abstract class BcBlockEntity extends BlockEntity {
   protected final ItemHandlerManager itemManager = new ItemHandlerManager((handler, slot, before, after) -> this.setChanged());
   private final Set<Player> usingPlayers = new HashSet<>();
   @Nullable
   private GameProfile owner;
   private boolean deferredPipeNeighborNotify = false;

   public BcBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
      super(type, pos, state);
   }

   public void onPlayerOpen(Player player) {
      this.usingPlayers.add(player);
   }

   public void onPlayerClose(Player player) {
      this.usingPlayers.remove(player);
   }

   public boolean canInteractWith(Player player) {
      return this.level != null && this.level.getBlockEntity(this.worldPosition) == this
         ? player.distanceToSqr(this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5) <= 64.0
         : false;
   }

   @Nullable
   public GameProfile getOwner() {
      return this.owner;
   }

   public void setOwner(@Nullable GameProfile owner) {
      this.owner = owner;
   }

   public void onPlacedBy(@Nullable LivingEntity placer, ItemStack stack) {
      if (placer instanceof Player player) {
         this.setOwner(player.getGameProfile());
         this.setChanged();
      }
   }

   protected void saveAdditional(ValueOutput output) {
      super.saveAdditional(output);
      if (this.owner != null && this.owner.id() != null) {
         output.putString("ownerUUID", this.owner.id().toString());
         if (this.owner.name() != null) {
            output.putString("ownerName", this.owner.name());
         }
      }
   }

   public void loadAdditional(ValueInput input) {
      super.loadAdditional(input);
      String uuidStr = input.getStringOr("ownerUUID", "");
      if (!uuidStr.isEmpty()) {
         try {
            UUID uuid = UUID.fromString(uuidStr);
            String name = input.getStringOr("ownerName", "Unknown");
            this.owner = new GameProfile(uuid, name);
         } catch (IllegalArgumentException e) {
            this.owner = null;
         }
      }
   }

   public void addDrops(NonNullList<ItemStack> toDrop, int fortune) {
      this.itemManager.addDrops(toDrop);
   }

   protected void notifyPipeNeighborConnections() {
      if (this.level == null || this.level.isClientSide()) {
         return;
      }

      Block block = this.getBlockState().getBlock();

      for (Direction direction : Direction.values()) {
         BlockPos adjPos = this.worldPosition.relative(direction);
         if (this.level.getBlockEntity(adjPos) instanceof IPipeHolder holder && holder.getPipe() != null) {
            holder.getPipe().markForUpdate();
            holder.wakePipe();
         }

         this.level.neighborChanged(adjPos, block, null);
      }
   }

   protected void schedulePipeNeighborNotify() {
      this.deferredPipeNeighborNotify = true;
   }

   protected void flushPipeNeighborNotify() {
      if (this.deferredPipeNeighborNotify) {
         this.deferredPipeNeighborNotify = false;
         this.notifyPipeNeighborConnections();
      }
   }

   @Nullable
   public Storage<ItemVariant> getSidedItemStorage(@Nullable Direction direction) {
      return direction == null ? null : this.itemManager.getItemStorage(direction);
   }

   public CompoundTag getUpdateTag(Provider registries) {
      return this.saveCustomOnly(registries);
   }

   @Nullable
   public Packet<ClientGamePacketListener> getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }
}

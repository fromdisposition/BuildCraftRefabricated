/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.tile;

import buildcraft.api.tiles.IDebuggable;
import buildcraft.lib.nbt.BcValueIn;
import buildcraft.lib.nbt.BcValueOut;
import buildcraft.lib.marker.MarkerCache;
import buildcraft.lib.marker.MarkerConnection;
import buildcraft.lib.marker.MarkerSubCache;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
//? if >= 1.21.10 {
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
//?}
import org.jetbrains.annotations.Nullable;

public abstract class TileMarker<C extends MarkerConnection<C>> extends BlockEntity implements IDebuggable {
   private boolean chunkUnloading = false;

   public TileMarker(BlockEntityType<?> type, BlockPos pos, BlockState state) {
      super(type, pos, state);
   }

   // Version-neutral serialization hooks; subclasses override writeData/readData (NOT saveAdditional).
   //? if >= 1.21.10 {
   protected void saveAdditional(ValueOutput output) {
      super.saveAdditional(output);
      this.writeData(new BcValueOut(output));
   }

   public void loadAdditional(ValueInput input) {
      super.loadAdditional(input);
      this.readData(new BcValueIn(input));
   }
   //?} else {
   /*protected void saveAdditional(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
      super.saveAdditional(tag, registries);
      this.writeData(new BcValueOut(tag, registries));
   }

   protected void loadAdditional(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
      super.loadAdditional(tag, registries);
      this.readData(new BcValueIn(tag, registries));
   }
   *///?}

   protected void writeData(BcValueOut output) {
   }

   protected void readData(BcValueIn input) {
   }

   public abstract MarkerCache<? extends MarkerSubCache<C>> getCache();

   public MarkerSubCache<C> getLocalCache() {
      return (MarkerSubCache<C>)this.getCache().getSubCache(this.level);
   }

   public abstract boolean isActiveForRender();

   public void onPlacedBy(@Nullable LivingEntity placer, ItemStack stack) {
   }

   public C getCurrentConnection() {
      return this.level == null ? null : this.getLocalCache().getConnection(this.getBlockPos());
   }

   public void buildcraft$onAttachedToLevel() {
      this.buildcraft$onAttachedToLevel(this.level);
   }

   public void buildcraft$onAttachedToLevel(Level level) {
      if (level != null && !level.isClientSide()) {
         this.getCache().getSubCache(level).loadMarker(this.getBlockPos(), this);
      }
   }

   @Override
   public void setLevel(Level level) {
      super.setLevel(level);
      if (level != null && !level.isClientSide()) {
         this.chunkUnloading = false;
         this.buildcraft$onAttachedToLevel(level);
      }
   }

   public void buildcraft$onChunkUnloading() {
      this.chunkUnloading = true;
      if (this.level != null && !this.level.isClientSide()) {
         this.getLocalCache().unloadMarker(this.getBlockPos());
      }
   }

   public void setRemoved() {
      super.setRemoved();
      if (this.level != null && !this.level.isClientSide() && !this.chunkUnloading) {
         this.getLocalCache().removeMarker(this.getBlockPos());
      }
   }

   protected void disconnectFromOthers() {
      C currentConnection = this.getCurrentConnection();
      if (currentConnection != null) {
         currentConnection.removeMarker(this.getBlockPos());
      }
   }

   @Override
   public void getDebugInfo(List<String> left, List<String> right, Direction side) {
      if (this.level != null) {
         C current = this.getCurrentConnection();
         MarkerSubCache<C> cache = this.getLocalCache();
         left.add("Exists = " + (cache.getMarker(this.getBlockPos()) == this));
         if (current == null) {
            left.add("Connection = null");
         } else {
            left.add("Connection:");
            current.getDebugInfo(this.getBlockPos(), left);
         }
      }
   }
}

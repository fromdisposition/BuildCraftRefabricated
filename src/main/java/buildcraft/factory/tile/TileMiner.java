/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.tile;

import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.tiles.IHasWork;
import buildcraft.core.BCCoreConfig;
import buildcraft.factory.BCFactoryBlocks;
import buildcraft.factory.client.render.TubeRenderer;
import buildcraft.lib.fabric.transfer.MjEnergyStorage;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.tile.BcBlockEntity;
import buildcraft.lib.tile.IBlockEntityLoadHook;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public abstract class TileMiner extends BcBlockEntity implements IHasWork, IBlockEntityLoadHook {
   protected int progress = 0;
   @Nullable
   protected BlockPos currentPos = null;
   protected int wantedLength = 0;
   protected double currentLength = 0.0;
   protected double lastLength = 0.0;
   private int offset;
   protected boolean isComplete = false;
   private boolean tubesCleared = false;
   protected final MjBattery battery = new MjBattery(this.getBatteryCapacity());

   public TileMiner(BlockEntityType<?> type, BlockPos pos, BlockState state) {
      super(type, pos, state);
   }

   protected abstract void mine();

   protected abstract IMjReceiver createMjReceiver();

   public void serverTick() {
      this.flushPipeNeighborNotify();
      this.battery.tick(this.getLevel(), this.getBlockPos());
      if (this.getLevel().getGameTime() % 10L == this.offset) {
         this.setChanged();
         MessageUtil.sendUpdateToTrackingPlayers(this);
      }

      this.mine();
   }

   public void clientTick() {
      this.lastLength = this.currentLength;
      if (Math.abs(this.wantedLength - this.currentLength) <= 0.01) {
         this.currentLength = this.wantedLength;
      } else {
         this.currentLength = this.currentLength + (this.wantedLength - this.currentLength) / 7.0;
      }
   }

   @Override
   public void onLoad() {
      if (this.level != null && this.level.getRandom() != null) {
         this.offset = this.level.getRandom().nextInt(10);
      }

      if (this.level != null && this.level.isClientSide()) {
         TubeRenderer.addMiner(this);
      } else if (this.level != null) {
         this.schedulePipeNeighborNotify();
      }
   }

   public void onRemove() {
      this.removeAllTubesInColumn();
   }

   protected void scanAndRemoveTubesInColumn() {
      if (this.level == null || this.level.isClientSide()) {
         return;
      }

      int maxDepth = BCCoreConfig.miningMaxDepth.get();
      int x = this.worldPosition.getX();
      int z = this.worldPosition.getZ();

      for (int y = this.worldPosition.getY() - 1; y > this.worldPosition.getY() - maxDepth; y--) {
         BlockPos blockPos = new BlockPos(x, y, z);
         if (this.level.getBlockState(blockPos).is(BCFactoryBlocks.TUBE)) {
            this.level.removeBlock(blockPos, false);
         }
      }
   }

   protected void removeAllTubesInColumn() {
      if (this.tubesCleared || this.level == null || this.level.isClientSide()) {
         return;
      }

      this.tubesCleared = true;
      this.scanAndRemoveTubesInColumn();
   }

   protected void removeTubesOutsideTip(int tipY) {
      if (this.level == null || this.level.isClientSide()) {
         return;
      }

      int pumpY = this.worldPosition.getY();
      int x = this.worldPosition.getX();
      int z = this.worldPosition.getZ();
      int minY = pumpY - BCCoreConfig.miningMaxDepth.get();

      for (int y = pumpY - 1; y > minY; y--) {
         if (y <= tipY) {
            BlockPos blockPos = new BlockPos(x, y, z);
            if (this.level.getBlockState(blockPos).is(BCFactoryBlocks.TUBE)) {
               this.level.removeBlock(blockPos, false);
            }
         }
      }
   }

   protected void syncMiningTubesToTip(int tipY) {
      if (this.level == null || this.level.isClientSide()) {
         return;
      }

      int pumpY = this.worldPosition.getY();
      int x = this.worldPosition.getX();
      int z = this.worldPosition.getZ();
      int minY = pumpY - BCCoreConfig.miningMaxDepth.get();

      for (int y = pumpY - 1; y > minY; y--) {
         BlockPos blockPos = new BlockPos(x, y, z);
         BlockState existing = this.level.getBlockState(blockPos);
         boolean wantTube = y > tipY && !existing.getFluidState().isSource();
         boolean isTube = existing.is(BCFactoryBlocks.TUBE);

         if (wantTube && !isTube) {
            this.level.setBlockAndUpdate(blockPos, BCFactoryBlocks.TUBE.defaultBlockState());
         } else if (!wantTube && isTube) {
            this.level.removeBlock(blockPos, false);
         }
      }
   }

   public void setRemoved() {
      super.setRemoved();
      if (this.level != null && this.level.isClientSide()) {
         TubeRenderer.removeMiner(this);
      }
   }

   protected void updateLength() {
      int newY = this.getTargetPos() != null ? this.getTargetPos().getY() : this.worldPosition.getY();
      int newLength = this.worldPosition.getY() - newY;
      if (newLength != this.wantedLength) {
         this.syncMiningTubesToTip(newY);
         this.currentLength = this.wantedLength = newLength;
         this.setChanged();
         MessageUtil.sendUpdateToTrackingPlayers(this);
      }
   }

   @Override
   public void preRemoveSideEffects(BlockPos pos, BlockState state) {
      if (this.level != null && !this.level.isClientSide()) {
         this.removeAllTubesInColumn();
      }

      super.preRemoveSideEffects(pos, state);
   }

   @Nullable
   protected BlockPos getTargetPos() {
      return this.currentPos;
   }

   public double getLength(float partialTicks) {
      if (partialTicks <= 0.0F) {
         return this.lastLength;
      } else {
         return partialTicks >= 1.0F ? this.currentLength : this.lastLength * (1.0F - partialTicks) + this.currentLength * partialTicks;
      }
   }

   public boolean isComplete() {
      return this.level != null && this.level.isClientSide() ? this.isComplete : this.currentPos == null;
   }

   @Override
   public boolean hasWork() {
      return !this.isComplete() && this.battery.getStored() > 0L;
   }

   public int getWantedLength() {
      return this.wantedLength;
   }

   public float getPercentFilledForRender() {
      float val = (float)this.battery.getStored() / (float)this.battery.getCapacity();
      return val < 0.0F ? 0.0F : (val > 1.0F ? 1.0F : val);
   }

   protected long getBatteryCapacity() {
      return 500L * MjAPI.MJ;
   }

   public IMjReceiver getMjReceiver() {
      return this.createMjReceiver();
   }

   public MjBattery getBattery() {
      return this.battery;
   }

   @Nullable
   public MjEnergyStorage getSidedEnergyStorage() {
      return MjEnergyStorage.createIfRfEnabled(this.battery);
   }

   @Override
   public CompoundTag getUpdateTag(Provider registries) {
      return this.saveCustomOnly(registries);
   }

   @Override
   public Packet<ClientGamePacketListener> getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   @Override
   protected void saveAdditional(ValueOutput output) {
      super.saveAdditional(output);
      if (this.currentPos != null) {
         output.putInt("currentPosX", this.currentPos.getX());
         output.putInt("currentPosY", this.currentPos.getY());
         output.putInt("currentPosZ", this.currentPos.getZ());
         output.putBoolean("hasCurrentPos", true);
      } else {
         output.putBoolean("hasCurrentPos", false);
      }

      output.putInt("wantedLength", this.wantedLength);
      output.putInt("progress", this.progress);
      output.putLong("mjStored", this.battery.getStored());
   }

   @Override
   public void loadAdditional(ValueInput input) {
      super.loadAdditional(input);
      if (input.getBooleanOr("hasCurrentPos", false)) {
         int x = input.getIntOr("currentPosX", 0);
         int y = input.getIntOr("currentPosY", 0);
         int z = input.getIntOr("currentPosZ", 0);
         this.currentPos = new BlockPos(x, y, z);
      } else {
         this.currentPos = null;
      }

      if (this.level != null && this.level.isClientSide()) {
         this.isComplete = this.currentPos == null;
      }

      int newWantedLength = input.getIntOr("wantedLength", 0);
      this.wantedLength = newWantedLength;
      this.progress = input.getIntOr("progress", 0);
      this.battery.extractPower(0L, Long.MAX_VALUE);
      this.battery.addPowerChecking(input.getLongOr("mjStored", 0L), false);
   }
}

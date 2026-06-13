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
import buildcraft.factory.BCFactoryEntities;
import buildcraft.factory.collision.MinerShaftCollisions;
import buildcraft.factory.entity.EntityMinerShaft;
import buildcraft.lib.fabric.transfer.MjEnergyStorage;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.phys.AABB;

public abstract class TileMiner extends BcBlockEntity implements IHasWork, IBlockEntityLoadHook {
   /** Shaft render scale in blocks per texture pixel row. */
   public static final double SHAFT_RADIUS = 0.0625D;
   public static final double SHAFT_CROSS_HALF = SHAFT_RADIUS * 4.0D;
   public static final double SHAFT_TOP_INSET = 0.001D;
   protected int progress = 0;
   @Nullable
   protected BlockPos currentPos = null;
   protected int wantedLength = 0;
   protected double currentLength = 0.0;
   protected double lastLength = 0.0;
   protected int offset;
   protected final MjBattery battery = new MjBattery(this.getBatteryCapacity());

   public TileMiner(BlockEntityType<?> type, BlockPos pos, BlockState state) {
      super(type, pos, state);
   }

   protected abstract void mine();

   protected abstract IMjReceiver createMjReceiver();

   protected int registeredCollisionLength = -1;
   @Nullable
   private EntityMinerShaft shaftRig;

   public void serverTick() {
      this.flushPipeNeighborNotify();
      this.battery.tick(this.getLevel(), this.getBlockPos());
      if (this.getLevel().getGameTime() % 10L == this.offset) {
         this.setChanged();
         if (this.level instanceof ServerLevel level) {
            Packet<?> packet = this.getUpdatePacket();
            if (packet != null) {
               for (ServerPlayer player : PlayerLookup.tracking(level, this.getBlockPos())) {
                  player.connection.send(packet);
               }
            }
         }
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

      if (this.level != null && !this.level.isClientSide()) {
         this.schedulePipeNeighborNotify();
         this.updateShaftCollision();
      }
   }

   /**
    * World position of the shaft bottom (where the tube visually ends).
    * {@link #getTargetPos()} is the active work target; the shaft end is derived from it per machine type.
    */
   @Nullable
   protected BlockPos getShaftEndPos() {
      BlockPos target = this.getTargetPos();
      return target != null ? this.resolveShaftEnd(target) : null;
   }

   /** Default: shaft stops above target. Pump overrides to reach into fluid. */
   protected BlockPos resolveShaftEnd(BlockPos target) {
      return target.above();
   }

   protected int getShaftLengthBlocks() {
      BlockPos end = this.getShaftEndPos();
      if (end == null) {
         return 0;
      }

      int length = this.worldPosition.getY() - end.getY();
      return length > 0 ? length : 0;
   }

   /** Collision may extend to the dig face while the renderer stops above it. */
   protected int getShaftCollisionLengthBlocks() {
      return this.getShaftLengthBlocks();
   }

   protected void updateShaftCollision() {
      if (this.level == null || this.level.isClientSide()) {
         return;
      }

      int length = this.getShaftCollisionLengthBlocks();
      if (length == this.registeredCollisionLength) {
         return;
      }

      this.registeredCollisionLength = length;
      AABB box = MinerShaftCollisions.box(this.worldPosition, length);
      if (box == null) {
         this.discardShaftRig();
         return;
      }

      this.ensureShaftRig();
      this.shaftRig.setShaftBox(box);
   }

   private void ensureShaftRig() {
      if (this.level == null || this.level.isClientSide()) {
         return;
      }

      if (this.shaftRig == null || this.shaftRig.isRemoved()) {
         this.shaftRig = new EntityMinerShaft(BCFactoryEntities.MINER_SHAFT, this.level);
         this.level.addFreshEntity(this.shaftRig);
      }
   }

   private void discardShaftRig() {
      if (this.shaftRig != null && !this.shaftRig.isRemoved()) {
         this.shaftRig.discard();
      }

      this.shaftRig = null;
   }

   protected void updateLength() {
      int newLength = this.getShaftLengthBlocks();
      if (newLength != this.wantedLength) {
         this.wantedLength = newLength;
         this.updateShaftCollision();
         this.setChanged();
         if (this.level instanceof ServerLevel level) {
            Packet<?> packet = this.getUpdatePacket();
            if (packet != null) {
               for (ServerPlayer player : PlayerLookup.tracking(level, this.getBlockPos())) {
                  player.connection.send(packet);
               }
            }
         }
      }
   }

   @Override
   public void preRemoveSideEffects(BlockPos pos, BlockState state) {
      this.registeredCollisionLength = -1;
      this.discardShaftRig();
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
      return this.currentPos == null;
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

      int newWantedLength = input.getIntOr("wantedLength", 0);
      this.wantedLength = newWantedLength;

      this.progress = input.getIntOr("progress", 0);
      this.battery.extractPower(0L, Long.MAX_VALUE);
      this.battery.addPowerChecking(input.getLongOr("mjStored", 0L), false);

      int computedLength = this.getShaftLengthBlocks();
      if (computedLength != this.wantedLength) {
         this.wantedLength = computedLength;
      }
   }
}

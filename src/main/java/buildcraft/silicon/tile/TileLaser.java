/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.tile;

import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.mj.ILaserTarget;
import buildcraft.api.mj.ILaserTargetBlock;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.lib.block.ILocalBlockUpdateSubscriber;
import buildcraft.lib.block.LocalBlockUpdateNotifier;
import buildcraft.lib.debug.IAdvDebugTarget;
import buildcraft.lib.fabric.transfer.MjEnergyStorage;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.VolumeUtil;
import buildcraft.lib.misc.data.AverageLong;
import buildcraft.lib.mj.MjBatteryReceiver;
import buildcraft.lib.tile.IBlockEntityLoadHook;
import buildcraft.silicon.BCSiliconBlockEntities;
import buildcraft.silicon.BCSiliconBlocks;
import buildcraft.silicon.block.BlockLaser;
import buildcraft.silicon.client.render.RenderLaser;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class TileLaser extends BlockEntity implements ILocalBlockUpdateSubscriber, IDebuggable, IAdvDebugTarget, IBlockEntityLoadHook {
   private static final int TARGETING_RANGE = 6;
   private final SafeTimeTracker clientLaserMoveInterval = new SafeTimeTracker(5L, 10L);
   private final SafeTimeTracker serverTargetMoveInterval = new SafeTimeTracker(10L, 20L);
   private final SafeTimeTracker rescanInterval = new SafeTimeTracker(40L, 20L);
   private final List<BlockPos> targetPositions = new ArrayList<>();
   private BlockPos targetPos;
   public Vec3 laserPos;
   private boolean worldHasUpdated = true;
   private final AverageLong avgPower = new AverageLong(100);
   private long averageClient;
   private final MjBattery battery;
   private final MjBatteryReceiver mjReceiver;
   private boolean registered = false;

   public TileLaser(BlockPos pos, BlockState state) {
      super(BCSiliconBlockEntities.LASER, pos, state);
      this.battery = new MjBattery(1024L * MjAPI.MJ);
      this.mjReceiver = new MjBatteryReceiver(this.battery);
   }

   public MjBatteryReceiver getMjReceiver() {
      return this.mjReceiver;
   }

   public MjBattery getBattery() {
      return this.battery;
   }

   public @Nullable MjEnergyStorage getSidedEnergyStorage() {
      return MjEnergyStorage.createIfRfEnabled(this.getBattery());
   }

   @Override
   public int getUpdateRange() {
      return 6;
   }

   @Override
   public BlockPos getSubscriberPos() {
      return this.getBlockPos();
   }

   @Override
   public void setLevelUpdated(Level world, BlockPos eventPos, BlockState oldState, BlockState newState, int flags) {
      this.worldHasUpdated = true;
   }

   private void findPossibleTargets() {
      this.targetPositions.clear();
      BlockState state = this.level.getBlockState(this.worldPosition);
      if (state.getBlock() == BCSiliconBlocks.LASER) {
         Direction face = (Direction)state.getValue(BlockLaser.FACING);
         VolumeUtil.iterateCone(this.level, this.worldPosition, face, 6, true, (w, s, p, visible) -> {
            if (visible) {
               BlockState stateAt = this.level.getBlockState(p);
               if (stateAt.getBlock() instanceof ILaserTargetBlock) {
                  BlockEntity tileAt = this.level.getBlockEntity(p);
                  if (tileAt instanceof ILaserTarget) {
                     this.targetPositions.add(p);
                  }
               }
            }
         });
      }
   }

   private void randomlyChooseTargetPos() {
      List<BlockPos> targetsNeedingPower = new ArrayList<>();

      for (BlockPos position : this.targetPositions) {
         if (this.isPowerNeededAt(position)) {
            targetsNeedingPower.add(position);
         }
      }

      if (targetsNeedingPower.isEmpty()) {
         this.targetPos = null;
      } else {
         this.targetPos = targetsNeedingPower.get(this.level.getRandom().nextInt(targetsNeedingPower.size()));
      }
   }

   private boolean isPowerNeededAt(BlockPos position) {
      return position != null && this.level.getBlockEntity(position) instanceof ILaserTarget target ? target.getRequiredLaserPower() > 0L : false;
   }

   private ILaserTarget getTarget() {
      if (this.targetPos != null) {
         BlockEntity tile = this.level.getBlockEntity(this.targetPos);
         if (tile instanceof ILaserTarget) {
            return (ILaserTarget)tile;
         }
      }

      return null;
   }

   private void updateLaser() {
      if (this.targetPos != null) {
         this.laserPos = Vec3.atLowerCornerOf(this.targetPos)
            .add((5 + this.level.getRandom().nextInt(6) + 0.5) / 16.0, 0.5625, (5 + this.level.getRandom().nextInt(6) + 0.5) / 16.0);
      } else {
         this.laserPos = null;
      }
   }

   public long getAverageClient() {
      return this.averageClient;
   }

   public long getMaxPowerPerTick() {
      return 4L * MjAPI.MJ;
   }

   public void clientTick() {
      if (this.clientLaserMoveInterval.markTimeIfDelay(this.level) || this.targetPos == null) {
         this.updateLaser();
      }
   }

   public void serverTick() {
      this.ensureRegistered();
      this.avgPower.tick();
      BlockPos previousTargetPos = this.targetPos;
      if (this.worldHasUpdated || this.rescanInterval.markTimeIfDelay(this.level)) {
         this.findPossibleTargets();
         this.worldHasUpdated = false;
      }

      ILaserTarget target = null;
      if (this.battery.getStored() <= 0L) {
         this.targetPos = null;
      } else {
         target = this.getTarget();
         boolean powerNeeded = target != null && target.getRequiredLaserPower() > 0L;
         if (!powerNeeded) {
            this.targetPos = null;
         }

         if (this.serverTargetMoveInterval.markTimeIfDelay(this.level) || !powerNeeded) {
            this.randomlyChooseTargetPos();
            target = this.getTarget();
         }
      }

      if (target != null) {
         long stored = this.battery.getStored();
         long max;
         if (stored <= 0L) {
            max = 0L;
         } else {
            long capacityHalf = this.battery.getCapacity() / 2L;
            max = capacityHalf <= 0L
               ? 0L
               : Math.min(this.getMaxPowerPerTick(), this.getMaxPowerPerTick() * stored / capacityHalf);
         }

         max = Math.min(max, target.getRequiredLaserPower());
         long power = this.battery.extractPower(0L, max);
         long excess = target.receiveLaserPower(power);
         if (excess > 0L) {
            this.battery.addPowerChecking(excess, false);
         }

         this.avgPower.push(power - excess);
      } else {
         this.avgPower.clear();
      }

      long previousAverageClient = this.averageClient;
      this.averageClient = (long)this.avgPower.getAverage();
      if (!Objects.equals(previousTargetPos, this.targetPos) || this.averageClient != previousAverageClient) {
         this.setChanged();
         MessageUtil.sendUpdateToTrackingPlayers(this);
      }
   }

   protected void saveAdditional(ValueOutput output) {
      super.saveAdditional(output);
      output.store("battery", CompoundTag.CODEC, this.battery.serializeNBT());
      if (this.laserPos != null) {
         output.putDouble("laser_x", this.laserPos.x);
         output.putDouble("laser_y", this.laserPos.y);
         output.putDouble("laser_z", this.laserPos.z);
      }

      if (this.targetPos != null) {
         output.putInt("target_x", this.targetPos.getX());
         output.putInt("target_y", this.targetPos.getY());
         output.putInt("target_z", this.targetPos.getZ());
         output.putBoolean("has_target", true);
      }

      CompoundTag avgTag = new CompoundTag();
      this.avgPower.writeToNbt(avgTag, "average_power");
      output.store("avg_power", CompoundTag.CODEC, avgTag);
   }

   public void loadAdditional(ValueInput input) {
      super.loadAdditional(input);
      input.read("battery", CompoundTag.CODEC).ifPresent(batteryTag -> this.battery.deserializeNBT(batteryTag));
      if (input.getBooleanOr("has_target", false)) {
         this.targetPos = new BlockPos(input.getIntOr("target_x", 0), input.getIntOr("target_y", 0), input.getIntOr("target_z", 0));
      } else {
         this.targetPos = null;
      }

      double lx = input.getDoubleOr("laser_x", Double.NaN);
      if (!Double.isNaN(lx)) {
         this.laserPos = new Vec3(lx, input.getDoubleOr("laser_y", 0.0), input.getDoubleOr("laser_z", 0.0));
      }

      input.read("avg_power", CompoundTag.CODEC).ifPresent(tag -> this.avgPower.readFromNbt(tag, "average_power"));
      this.averageClient = (long)this.avgPower.getAverage();
   }

   public CompoundTag getUpdateTag(Provider registries) {
      return this.saveCustomOnly(registries);
   }

   public Packet<ClientGamePacketListener> getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   @Override
   public void onLoad() {
      if (this.level != null && this.level.isClientSide()) {
         RenderLaser.addLaser(this);
      } else if (this.level != null) {
         this.worldHasUpdated = true;
         this.ensureRegistered();
      }
   }

   public void setRemoved() {
      super.setRemoved();
      if (this.level != null && !this.level.isClientSide()) {
         LocalBlockUpdateNotifier.instance(this.level).removeSubscriberFromUpdateNotifications(this);
      }

      if (this.level != null && this.level.isClientSide()) {
         RenderLaser.removeLaser(this);
      }
   }

   private void ensureRegistered() {
      if (!this.registered && this.level != null && !this.level.isClientSide()) {
         LocalBlockUpdateNotifier.instance(this.level).registerSubscriberForUpdateNotifications(this);
         this.registered = true;
      }
   }

   @Override
   public void getDebugInfo(List<String> left, List<String> right, Direction side) {
      left.add("battery = " + this.battery.getStored() + " / " + this.battery.getCapacity());
      left.add("target = " + this.targetPos);
      left.add("laser = " + this.laserPos);
      left.add("average = " + this.averageClient);
      if (this.level != null && this.level.isClientSide()) {
         left.add("active_lasers = " + RenderLaser.getActiveCount());
      }
   }

   @Override
   public Component getAdvDebugMessage() {
      return Component.translatable("chat.debugger.laser");
   }
}

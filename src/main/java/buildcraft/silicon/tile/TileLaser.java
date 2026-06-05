/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.tile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.mj.ILaserTarget;
import buildcraft.api.mj.ILaserTargetBlock;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;

import buildcraft.lib.block.ILocalBlockUpdateSubscriber;
import buildcraft.lib.block.LocalBlockUpdateNotifier;
import buildcraft.lib.debug.IAdvDebugTarget;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.VolumeUtil;
import buildcraft.lib.misc.data.AverageLong;
import buildcraft.lib.mj.MjBatteryReceiver;

import buildcraft.api.tiles.IDebuggable;
import buildcraft.silicon.BCSiliconBlockEntities;
import buildcraft.silicon.BCSiliconBlocks;
import buildcraft.silicon.block.BlockLaser;

public class TileLaser extends BlockEntity implements ILocalBlockUpdateSubscriber, IDebuggable, IAdvDebugTarget,
        buildcraft.lib.tile.IBlockEntityLoadHook {
    private static final int TARGETING_RANGE = 6;

    private final SafeTimeTracker clientLaserMoveInterval = new SafeTimeTracker(5, 10);
    private final SafeTimeTracker serverTargetMoveInterval = new SafeTimeTracker(10, 20);
    private final SafeTimeTracker rescanInterval = new SafeTimeTracker(40, 20);

    private final List<BlockPos> targetPositions = new ArrayList<>();
    private BlockPos targetPos;
    public Vec3 laserPos;
    private boolean worldHasUpdated = true;

    private final AverageLong avgPower = new AverageLong(100);
    private long averageClient;
    private final MjBattery battery;
    private final MjBatteryReceiver mjReceiver;

    public TileLaser(BlockPos pos, BlockState state) {
        super(BCSiliconBlockEntities.LASER, pos, state);
        battery = new MjBattery(1024 * MjAPI.MJ);
        mjReceiver = new MjBatteryReceiver(battery);
    }

    public MjBatteryReceiver getMjReceiver() {
        return mjReceiver;
    }

    public MjBattery getBattery() {
        return battery;
    }

    @Override
    public int getUpdateRange() {
        return TARGETING_RANGE;
    }

    @Override
    public BlockPos getSubscriberPos() {
        return getBlockPos();
    }

    @Override
    public void setLevelUpdated(Level world, BlockPos eventPos, BlockState oldState, BlockState newState, int flags) {
        this.worldHasUpdated = true;
    }

    private void findPossibleTargets() {
        targetPositions.clear();
        BlockState state = level.getBlockState(worldPosition);
        if (state.getBlock() != BCSiliconBlocks.LASER.get()) {
            return;
        }
        Direction face = state.getValue(BlockLaser.FACING);

        VolumeUtil.iterateCone(level, worldPosition, face, TARGETING_RANGE, true, (w, s, p, visible) -> {
            if (!visible) {
                return;
            }
            BlockState stateAt = level.getBlockState(p);
            if (stateAt.getBlock() instanceof ILaserTargetBlock) {
                BlockEntity tileAt = level.getBlockEntity(p);
                if (tileAt instanceof ILaserTarget) {
                    targetPositions.add(p);
                }
            }
        });
    }

    private void randomlyChooseTargetPos() {
        List<BlockPos> targetsNeedingPower = new ArrayList<>();
        for (BlockPos position : targetPositions) {
            if (isPowerNeededAt(position)) {
                targetsNeedingPower.add(position);
            }
        }
        if (targetsNeedingPower.isEmpty()) {
            targetPos = null;
            return;
        }
        targetPos = targetsNeedingPower.get(level.getRandom().nextInt(targetsNeedingPower.size()));
    }

    private boolean isPowerNeededAt(BlockPos position) {
        if (position != null) {
            BlockEntity tile = level.getBlockEntity(position);
            if (tile instanceof ILaserTarget target) {
                return target.getRequiredLaserPower() > 0;
            }
        }
        return false;
    }

    private ILaserTarget getTarget() {
        if (targetPos != null) {
            BlockEntity tile = level.getBlockEntity(targetPos);
            if (tile instanceof ILaserTarget) {
                return (ILaserTarget) tile;
            }
        }
        return null;
    }

    private void updateLaser() {
        if (targetPos != null) {
            laserPos = Vec3.atLowerCornerOf(targetPos).add(
                (5 + level.getRandom().nextInt(6) + 0.5) / 16D,
                9 / 16D,
                (5 + level.getRandom().nextInt(6) + 0.5) / 16D
            );
        } else {
            laserPos = null;
        }
    }

    public long getAverageClient() {
        return averageClient;
    }

    public long getMaxPowerPerTick() {
        return 4 * MjAPI.MJ;
    }

    public void clientTick() {
        if (clientLaserMoveInterval.markTimeIfDelay(level) || targetPos == null) {
            updateLaser();
        }
    }

    public void serverTick() {
        ensureRegistered();
        avgPower.tick();

        BlockPos previousTargetPos = targetPos;
        if (worldHasUpdated || rescanInterval.markTimeIfDelay(level)) {
            findPossibleTargets();
            worldHasUpdated = false;
        }

        if (battery.getStored() <= 0) {
            targetPos = null;
        } else {
            if (!isPowerNeededAt(targetPos)) {
                targetPos = null;
            }

            if (serverTargetMoveInterval.markTimeIfDelay(level) || !isPowerNeededAt(targetPos)) {
                randomlyChooseTargetPos();
            }
        }

        ILaserTarget target = getTarget();
        if (target != null) {
            long max = getMaxPowerPerTick();
            max *= battery.getStored() + max;
            max /= battery.getCapacity() / 2;
            max = Math.min(Math.min(max, getMaxPowerPerTick()), target.getRequiredLaserPower());
            long power = battery.extractPower(0, max);
            long excess = target.receiveLaserPower(power);
            if (excess > 0) {
                battery.addPowerChecking(excess, false);
            }
            avgPower.push(power - excess);
        } else {
            avgPower.clear();
        }

        averageClient = (long) avgPower.getAverage();

        if (!Objects.equals(previousTargetPos, targetPos) || true) {

            setChanged();
            MessageUtil.sendUpdateToTrackingPlayers(this);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("battery", CompoundTag.CODEC, battery.serializeNBT());
        if (laserPos != null) {
            output.putDouble("laser_x", laserPos.x);
            output.putDouble("laser_y", laserPos.y);
            output.putDouble("laser_z", laserPos.z);
        }
        if (targetPos != null) {
            output.putInt("target_x", targetPos.getX());
            output.putInt("target_y", targetPos.getY());
            output.putInt("target_z", targetPos.getZ());
            output.putBoolean("has_target", true);
        }
        CompoundTag avgTag = new CompoundTag();
        avgPower.writeToNbt(avgTag, "average_power");
        output.store("avg_power", CompoundTag.CODEC, avgTag);
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.read("battery", CompoundTag.CODEC).ifPresent(batteryTag -> battery.deserializeNBT(batteryTag));
        if (input.getBooleanOr("has_target", false)) {
            targetPos = new BlockPos(
                input.getIntOr("target_x", 0),
                input.getIntOr("target_y", 0),
                input.getIntOr("target_z", 0)
            );
        } else {
            targetPos = null;
        }
        double lx = input.getDoubleOr("laser_x", Double.NaN);
        if (!Double.isNaN(lx)) {
            laserPos = new Vec3(
                lx,
                input.getDoubleOr("laser_y", 0),
                input.getDoubleOr("laser_z", 0)
            );
        }
        input.read("avg_power", CompoundTag.CODEC)
            .ifPresent(tag -> avgPower.readFromNbt(tag, "average_power"));
        averageClient = (long) avgPower.getAverage();
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void onLoad() {
        if (level != null && level.isClientSide()) {
            buildcraft.silicon.client.render.RenderLaser.addLaser(this);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null && !level.isClientSide()) {
            LocalBlockUpdateNotifier.instance(level).removeSubscriberFromUpdateNotifications(this);
        }
        if (level != null && level.isClientSide()) {
            buildcraft.silicon.client.render.RenderLaser.removeLaser(this);
        }
    }

    private boolean registered = false;
    private void ensureRegistered() {
        if (!registered && level != null && !level.isClientSide()) {
            LocalBlockUpdateNotifier.instance(level).registerSubscriberForUpdateNotifications(this);
            registered = true;
        }
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, Direction side) {
        left.add("battery = " + battery.getStored() + " / " + battery.getCapacity());
        left.add("target = " + targetPos);
        left.add("laser = " + laserPos);
        left.add("average = " + averageClient);
        if (level != null && level.isClientSide()) {
            left.add("active_lasers = " + buildcraft.silicon.client.render.RenderLaser.getActiveCount());
        }
    }

    @Override
    public Component getAdvDebugMessage() {
        return Component.translatable("chat.debugger.laser");
    }
}

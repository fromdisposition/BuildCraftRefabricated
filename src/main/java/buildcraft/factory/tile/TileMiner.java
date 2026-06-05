/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.tile;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.tiles.IHasWork;

import buildcraft.core.BCCoreConfig;
import buildcraft.factory.BCFactoryBlocks;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.tile.TileBC_Neptune;

@SuppressWarnings("this-escape")
public abstract class TileMiner extends TileBC_Neptune
        implements IHasWork, buildcraft.lib.tile.IBlockEntityLoadHook {

    protected int progress = 0;
    @Nullable
    protected BlockPos currentPos = null;

    protected int wantedLength = 0;
    protected double currentLength = 0;
    protected double lastLength = 0;
    private int offset;

    protected boolean isComplete = false;
    protected final MjBattery battery = new MjBattery(getBatteryCapacity());

    public TileMiner(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected abstract void mine();

    protected abstract IMjReceiver createMjReceiver();

    public void serverTick() {
        battery.tick(getLevel(), getBlockPos());

        if (getLevel().getGameTime() % 10 == offset) {
            setChanged();
            MessageUtil.sendUpdateToTrackingPlayers(this);
        }

        mine();
    }

    public void clientTick() {
        lastLength = currentLength;
        if (Math.abs(wantedLength - currentLength) <= 0.01) {
            currentLength = wantedLength;
        } else {
            currentLength = currentLength + (wantedLength - currentLength) / 7D;
        }
    }

    public void onLoad() {
        if (level != null && level.getRandom() != null) {
            offset = level.getRandom().nextInt(10);
        }
        if (level != null && level.isClientSide()) {
            buildcraft.factory.client.render.TubeRenderer.addMiner(this);
        }
    }

    public void onRemove() {
        for (int y = worldPosition.getY() - 1; y > worldPosition.getY() - BCCoreConfig.miningMaxDepth.get(); y--) {
            BlockPos blockPos = new BlockPos(worldPosition.getX(), y, worldPosition.getZ());
            if (level.getBlockState(blockPos).is(BCFactoryBlocks.TUBE)) {
                level.removeBlock(blockPos, false);
            } else {
                break;
            }
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null && level.isClientSide()) {
            buildcraft.factory.client.render.TubeRenderer.removeMiner(this);
        }
    }

    protected void updateLength() {
        int newY = getTargetPos() != null ? getTargetPos().getY() : worldPosition.getY();
        int newLength = worldPosition.getY() - newY;
        if (newLength != wantedLength) {

            for (int y = worldPosition.getY() - 1; y > worldPosition.getY() - BCCoreConfig.miningMaxDepth.get(); y--) {
                BlockPos blockPos = new BlockPos(worldPosition.getX(), y, worldPosition.getZ());
                if (level.getBlockState(blockPos).is(BCFactoryBlocks.TUBE)) {
                    level.removeBlock(blockPos, false);
                } else {
                    break;
                }
            }

            for (int y = worldPosition.getY() - 1; y > newY; y--) {
                BlockPos blockPos = new BlockPos(worldPosition.getX(), y, worldPosition.getZ());
                level.setBlockAndUpdate(blockPos, BCFactoryBlocks.TUBE.defaultBlockState());
            }
            currentLength = wantedLength = newLength;
            setChanged();
            MessageUtil.sendUpdateToTrackingPlayers(this);
        }
    }

    @Nullable
    protected BlockPos getTargetPos() {
        return currentPos;
    }

    public double getLength(float partialTicks) {
        if (partialTicks <= 0) {
            return lastLength;
        } else if (partialTicks >= 1) {
            return currentLength;
        } else {
            return lastLength * (1 - partialTicks) + currentLength * partialTicks;
        }
    }

    public boolean isComplete() {
        return level != null && level.isClientSide() ? isComplete : currentPos == null;
    }

    @Override
    public boolean hasWork() {
        return !isComplete();
    }

    public int getWantedLength() {
        return wantedLength;
    }

    public float getPercentFilledForRender() {
        float val = battery.getStored() / (float) battery.getCapacity();
        return val < 0 ? 0 : val > 1 ? 1 : val;
    }

    protected long getBatteryCapacity() {
        return 500 * MjAPI.MJ;
    }

    public IMjReceiver getMjReceiver() {
        return createMjReceiver();
    }

    public MjBattery getBattery() {
        return battery;
    }

    @Override
    public net.minecraft.nbt.CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {

        return this.saveCustomOnly(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (currentPos != null) {
            output.putInt("currentPosX", currentPos.getX());
            output.putInt("currentPosY", currentPos.getY());
            output.putInt("currentPosZ", currentPos.getZ());
            output.putBoolean("hasCurrentPos", true);
        } else {
            output.putBoolean("hasCurrentPos", false);
        }
        output.putInt("wantedLength", wantedLength);
        output.putInt("progress", progress);
        output.putLong("mjStored", battery.getStored());
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        if (input.getBooleanOr("hasCurrentPos", false)) {
            int x = input.getIntOr("currentPosX", 0);
            int y = input.getIntOr("currentPosY", 0);
            int z = input.getIntOr("currentPosZ", 0);
            currentPos = new BlockPos(x, y, z);
        } else {
            currentPos = null;
        }

        if (level != null && level.isClientSide()) {
            isComplete = (currentPos == null);
        }
        int newWantedLength = input.getIntOr("wantedLength", 0);

        wantedLength = newWantedLength;
        progress = input.getIntOr("progress", 0);

        battery.extractPower(0, Long.MAX_VALUE);
        battery.addPowerChecking(input.getLongOr("mjStored", 0L), false);
    }
}

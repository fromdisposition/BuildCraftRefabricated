/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.lib.engine;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.authlib.GameProfile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import buildcraft.api.enums.EnumPowerStage;
import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.IMjRedstoneReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjToRfAutoConvertor;
import buildcraft.api.tiles.IDebuggable;

import buildcraft.lib.BCLibConfig;
import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.misc.LocaleUtil;

import buildcraft.lib.attachments.Attachments;
import buildcraft.lib.attachments.AttachmentQueries;
import buildcraft.lib.transfer.energy.EnergyHandler;

public abstract class TileEngineBase_BC8 extends BlockEntity implements IDebuggable {

    public static final Identifier ADVANCEMENT_TO_MUCH_POWER =
        Identifier.parse("buildcraftenergy:to_much_power");

    public static final float MIN_HEAT = 20f;
    public static final float MAX_HEAT = 250f;

    @Nullable
    private GameProfile owner;

    protected Direction orientation = Direction.UP;
    protected long power = 0;
    public long currentOutput = 0;

    public long getPower() { return power; }
    protected float heat = MIN_HEAT;
    protected float progress = 0;
    protected int progressPart = 0;
    protected boolean isPumping = false;
    protected boolean isRedstonePowered = false;

    private float lastProgress = 0;
    private float clientProgress = 0;
    private boolean clientIsPumping = false;

    Direction prevOrientation = Direction.UP;
    boolean prevIsPumping = false;
    EnumPowerStage prevPowerStage = EnumPowerStage.BLUE;

    protected int orientationChecksRemaining = 1;
    protected boolean checkRedstonePower = true;
    protected int redstonePollTimer = 0;

    private EnumPowerStage powerStage = EnumPowerStage.BLUE;

    public final buildcraft.lib.misc.data.ModelVariableData clientModelData = new buildcraft.lib.misc.data.ModelVariableData();

    private IMjConnector mjConnector;

    public TileEngineBase_BC8(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public abstract boolean isBurning();

    protected abstract void engineUpdate();

    public abstract long getMaxPower();

    public abstract long minPowerReceived();

    public abstract long maxPowerReceived();

    public abstract long maxPowerExtracted();

    public abstract long getCurrentOutput();

    public abstract float explosionRange();

    @Nonnull
    protected abstract IMjConnector createConnector();

    public void onPlacedBy(@Nullable LivingEntity placer, net.minecraft.world.item.ItemStack stack) {
        if (placer instanceof net.minecraft.world.entity.player.Player player) {
            owner = player.getGameProfile();
        }
        if (getBlockState().hasProperty(buildcraft.api.properties.BuildCraftProperties.BLOCK_FACING_6)) {
            setOrientation(getBlockState().getValue(buildcraft.api.properties.BuildCraftProperties.BLOCK_FACING_6));
        }
    }

    @Nullable
    public GameProfile getOwner() {
        return owner;
    }

    public void setOwner(@Nullable GameProfile owner) {
        this.owner = owner;
    }

    protected int getMaxChainLength() {
        return 2;
    }

    public double getPistonSpeed() {
        switch (getPowerStage()) {
            case BLUE:   return 0.02;
            case GREEN:  return 0.04;
            case YELLOW: return 0.08;
            case RED:    return 0.12;
            default:     return 0;
        }
    }

    public void updateHeatLevel() {
        heat = (float) (((MAX_HEAT - MIN_HEAT) * getEnergyLevel()) + MIN_HEAT);
    }

    protected EnumPowerStage computePowerStage() {
        float heatLevel = getHeatLevel();
        if (heatLevel < 0.25f) return EnumPowerStage.BLUE;
        if (heatLevel < 0.5f) return EnumPowerStage.GREEN;
        if (heatLevel < 0.75f) return EnumPowerStage.YELLOW;
        if (heatLevel < 0.85f) return EnumPowerStage.RED;
        return EnumPowerStage.OVERHEAT;
    }

    public float getHeat() {
        return heat;
    }

    public float getHeatLevel() {
        return (heat - MIN_HEAT) / (MAX_HEAT - MIN_HEAT);
    }

    public double getEnergyLevel() {
        long max = getMaxPower();
        if (max <= 0) return 0;
        return (double) power / max;
    }

    public final EnumPowerStage getPowerStage() {
        if (level != null && !level.isClientSide()) {
            if (powerStage == EnumPowerStage.OVERHEAT) {
                return powerStage;
            }
            EnumPowerStage newStage = computePowerStage();
            if (powerStage != newStage) {
                powerStage = newStage;
                if (powerStage == EnumPowerStage.OVERHEAT) {
                    overheat();
                }
                setChanged();
            }
        }
        return powerStage;
    }

    protected void overheat() {
        isPumping = false;
        if (!BCLibConfig.canEnginesExplode.get()) return;
        float range = explosionRange();
        if (range > 0 && level != null) {
            level.explode(null, getBlockPos().getX() + 0.5, getBlockPos().getY() + 0.5,
                getBlockPos().getZ() + 0.5, range, Level.ExplosionInteraction.BLOCK);
            level.removeBlock(getBlockPos(), false);
        }
    }

    public boolean clearOverheat(@Nullable Player player) {
        if (powerStage != EnumPowerStage.OVERHEAT) return false;
        heat = MIN_HEAT;
        powerStage = computePowerStage();
        isPumping = false;
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            if (player != null) {
                AdvancementUtil.unlockAdvancement(player, ADVANCEMENT_TO_MUCH_POWER);
            }
        }
        return true;
    }

    public boolean hasAlternateReceiver() {
        for (Direction d : Direction.values()) {
            if (d == orientation) continue;
            if (getReceiverToPower(d) != null) return true;
        }
        return false;
    }

    public IMjConnector getMjConnector() {
        if (mjConnector == null) {
            mjConnector = createConnector();
        }
        return mjConnector;
    }

    public long extractPower(long min, long max, boolean doExtract) {
        if (power < min) return 0;
        long actualMax = Math.min(max, maxPowerExtracted());
        if (actualMax < min) return 0;
        long extracted = Math.min(power, actualMax);
        if (doExtract) {
            power -= extracted;
        }
        return extracted;
    }

    @Nullable
    public IMjReceiver getReceiverToPower(Direction side) {
        if (level == null) return null;

        BlockPos pos = getBlockPos();
        for (int len = 0; len <= getMaxChainLength(); len++) {
            BlockPos targetPos = pos.relative(side);
            BlockEntity tile = level.getBlockEntity(targetPos);
            if (tile == null) {
                return null;
            }
            if (tile.getClass() == getClass()) {

                if (((TileEngineBase_BC8) tile).orientation != side) {
                    return null;
                }
                pos = targetPos;
                continue;
            }

            IMjReceiver receiver = AttachmentQueries.getBlock(level, MjAPI.CAP_RECEIVER, targetPos, side.getOpposite());
            if (receiver != null && receiver.canConnect(getMjConnector()) && getMjConnector().canConnect(receiver)) {
                return receiver;
            }

            EnergyHandler feHandler = AttachmentQueries.getBlock(level, Attachments.Energy.BLOCK, targetPos, side.getOpposite());
            if (feHandler != null) {
                IMjReceiver feReceiver = MjToRfAutoConvertor.createReceiver(feHandler);
                if (feReceiver != null && feReceiver.canConnect(getMjConnector())) {
                    return feReceiver;
                }
            }
            return null;
        }

        return null;
    }

    protected void sendPower(@Nullable IMjReceiver receiver) {
        if (receiver == null) {
            currentOutput = 0;
            return;
        }
        long requested = receiver.getPowerRequested();
        long extracted = extractPower(0, requested, false);
        if (extracted > 0) {
            long excess = receiver.receivePower(extracted, false);
            long actualSent = extracted - excess;
            extractPower(actualSent, actualSent, true);
            currentOutput = actualSent;
        } else {
            currentOutput = 0;
        }
    }

    public static <T extends TileEngineBase_BC8> void serverTick(Level level, BlockPos pos, BlockState state, T engine) {
        ProfilerFiller _profiler = Profiler.get();
        _profiler.push("buildcraft:engine_serverTick");
        try {

        engine.redstonePollTimer++;
        if (engine.redstonePollTimer >= 10) {
            engine.redstonePollTimer = 0;
            engine.checkRedstonePower = true;
        }

        if (engine.checkRedstonePower) {
            engine.checkRedstoneLevel();
        }

        if (engine.orientationChecksRemaining > 0) {
            engine.orientationChecksRemaining--;

            if (engine.getReceiverToPower(engine.orientation) == null) {

                if (engine.attemptRotation()) {
                    engine.orientationChecksRemaining = 0;
                    level.setBlock(pos, state.setValue(
                            buildcraft.api.properties.BuildCraftProperties.BLOCK_FACING_6,
                            engine.orientation), 3);
                    level.sendBlockUpdated(pos, state, state, 3);
                }
            } else {

                engine.orientationChecksRemaining = 0;
            }
        }

        engine.updateHeatLevel();
        engine.getPowerStage();

        if (engine.getPowerStage() == EnumPowerStage.OVERHEAT) {
            engine.power = Math.max(engine.power - 10, 0);
            return;
        }

        if (!engine.isRedstonePowered) {
            if (engine.power > MjAPI.MJ) {
                engine.power -= MjAPI.MJ;
            } else if (engine.power > 0) {
                engine.power = 0;
            }
        }

        engine.engineUpdate();

        IMjReceiver receiver = engine.getReceiverToPower(engine.orientation);
        boolean pulsedPower = receiver instanceof IMjRedstoneReceiver;

        if (engine.progressPart != 0) {
            engine.progress += (float) engine.getPistonSpeed();
            if (engine.progress > 0.5f && engine.progressPart == 1) {
                engine.progressPart = 2;

                if (pulsedPower) {
                    engine.sendPower(receiver);
                }
            } else if (engine.progress >= 1.0f) {
                engine.progress = 0;
                engine.progressPart = 0;
            }
        } else if (engine.isRedstonePowered && engine.isBurning() && receiver != null) {
            long requested = receiver.getPowerRequested();
            if (requested > 0 && engine.extractPower(0, requested, false) > 0) {
                engine.progressPart = 1;
                engine.setPumping(true);
            } else {
                engine.setPumping(false);
            }
        } else {
            engine.setPumping(false);
        }

        if (!pulsedPower) {
            if (engine.isRedstonePowered && engine.isBurning()) {
                engine.sendPower(receiver);
            } else {
                engine.currentOutput = 0;
            }
        }

        engine.setChanged();

        boolean needsSync = false;
        if (engine.orientation != engine.prevOrientation) {
            engine.prevOrientation = engine.orientation;
            needsSync = true;
        }
        if (engine.isPumping != engine.prevIsPumping) {
            engine.prevIsPumping = engine.isPumping;
            needsSync = true;
        }
        if (engine.getPowerStage() != engine.prevPowerStage) {
            engine.prevPowerStage = engine.getPowerStage();
            needsSync = true;
        }
        if (needsSync) {
            level.sendBlockUpdated(pos, state, state, 3);
        }
        } finally {
            _profiler.pop();
        }
    }

    public void checkRedstoneLevel() {
        checkRedstonePower = false;
        if (level != null) {
            isRedstonePowered = level.hasNeighborSignal(getBlockPos());
        }
    }

    public void onNeighborUpdate() {
        checkRedstonePower = true;

        orientationChecksRemaining = 5;
    }

    protected final void setPumping(boolean active) {
        if (isPumping == active) return;
        isPumping = active;
        setChanged();
    }

    public boolean isPumping() {
        return isPumping;
    }

    public Direction getOrientation() {
        return orientation;
    }

    public void setOrientation(Direction dir) {
        orientation = dir;
        orientationChecksRemaining = 1;
        setChanged();
    }

    public void rotateOrientation() {
        int next = (orientation.ordinal() + 1) % 6;
        setOrientation(Direction.values()[next]);
    }

    public boolean attemptRotation() {
        Direction current = orientation;
        Direction[] dirs = Direction.values();
        for (int i = 0; i < 6; i++) {
            current = dirs[(current.ordinal() + 1) % 6];
            if (isFacingReceiver(current)) {
                if (current != orientation) {
                    setOrientation(current);
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    private boolean isFacingReceiver(Direction dir) {
        return getReceiverToPower(dir) != null;
    }

    public void clientTick() {
        lastProgress = clientProgress;
        clientIsPumping = isPumping;
        if (clientIsPumping) {
            clientProgress += (float) getPistonSpeed();
            if (clientProgress >= 1.0f) {
                clientProgress = 0;
            }
        } else {

            if (clientProgress > 0) {
                clientProgress -= 0.02f;
                if (clientProgress < 0) clientProgress = 0;
            }
        }
    }

    public float getProgressClient(float partialTicks) {

        if (lastProgress > 0.8f && clientProgress < 0.2f) {

            float interp = lastProgress + (1.0f + clientProgress - lastProgress) * partialTicks;
            return interp >= 1.0f ? interp - 1.0f : interp;
        }
        return lastProgress + (clientProgress - lastProgress) * partialTicks;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void getDebugInfo(java.util.List<String> left, java.util.List<String> right, Direction side) {
        left.add("facing = " + orientation);
        left.add("heat = " + LocaleUtil.localizeHeat(heat) + " -- " + String.format("%.2f %%", getHeatLevel() * 100f));
        left.add("power = " + LocaleUtil.localizeMj(power));
        left.add("stage = " + getPowerStage());
        left.add("progress = " + progress);
        left.add("last = " + LocaleUtil.localizeMjFlow(currentOutput));
    }

    @Override
    public void getClientDebugInfo(java.util.List<String> left, java.util.List<String> right, Direction side) {
        left.add("Current Model Variables:");
        clientModelData.addDebugInfo(left);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putByte("orientation", (byte) orientation.ordinal());
        output.putLong("power", power);
        output.putFloat("heat", heat);
        output.putFloat("progress", progress);
        output.putBoolean("isPumping", isPumping);
        output.putBoolean("isRedstonePowered", isRedstonePowered);
        output.putByte("powerStage", (byte) powerStage.ordinal());

        if (owner != null && owner.id() != null) {
            output.putString("ownerUUID", owner.id().toString());
            if (owner.name() != null) {
                output.putString("ownerName", owner.name());
            }
        }
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        int ord = input.getByteOr("orientation", (byte) Direction.UP.ordinal());
        orientation = Direction.values()[Math.min(ord, 5)];
        power = input.getLongOr("power", 0L);
        heat = input.getFloatOr("heat", MIN_HEAT);
        progress = input.getFloatOr("progress", 0f);
        isPumping = input.getBooleanOr("isPumping", false);
        isRedstonePowered = input.getBooleanOr("isRedstonePowered", false);
        int ps = input.getByteOr("powerStage", (byte) 0);
        powerStage = EnumPowerStage.VALUES[Math.min(ps, EnumPowerStage.VALUES.length - 1)];

        String uuidStr = input.getStringOr("ownerUUID", "");
        if (!uuidStr.isEmpty()) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                String name = input.getStringOr("ownerName", "Unknown");
                owner = new GameProfile(uuid, name);
            } catch (IllegalArgumentException e) {
                owner = null;
            }
        }
    }
}

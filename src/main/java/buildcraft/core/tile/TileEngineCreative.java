/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core.tile;

import javax.annotation.Nonnull;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.core.Direction;

import buildcraft.api.enums.EnumPowerStage;
import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.tools.IToolWrench;

import buildcraft.core.BCCoreBlockEntities;
import buildcraft.lib.engine.EngineConnector;
import buildcraft.lib.engine.TileEngineBase_BC8;
import buildcraft.lib.misc.MathUtil;

public class TileEngineCreative extends TileEngineBase_BC8 {
    public static final long[] OUTPUTS = { 1, 2, 4, 8, 16, 32, 64, 128, 256 };
    public int currentOutputIndex = 0;

    public TileEngineCreative(BlockPos pos, BlockState state) {
        super(BCCoreBlockEntities.ENGINE_CREATIVE, pos, state);
    }

    @Nonnull
    @Override
    protected IMjConnector createConnector() {
        return new EngineConnector(false);
    }

    @Override
    public boolean isBurning() {
        return isRedstonePowered;
    }

    @Override
    protected void engineUpdate() {
        if (isBurning()) {
            power += getCurrentOutput();
            long max = getMaxPower();
            if (power > max) {
                power = max;
            }
        } else {
            power = 0;
        }
    }

    @Override
    public double getPistonSpeed() {
        final double max = 0.08;
        final double min = 0.01;
        double interp = currentOutputIndex / (double) (OUTPUTS.length - 1);
        return MathUtil.interp(interp, min, max);
    }

    @Override
    protected EnumPowerStage computePowerStage() {
        return EnumPowerStage.BLACK;
    }

    @Override
    public long getMaxPower() {
        return getCurrentOutput() * 10_000;
    }

    @Override
    public long minPowerReceived() {
        return 0;
    }

    @Override
    public long maxPowerReceived() {
        return 2_000 * MjAPI.MJ;
    }

    @Override
    public long maxPowerExtracted() {
        return 20 * getCurrentOutput();
    }

    @Override
    public float explosionRange() {
        return 0;
    }

    @Override
    public long getCurrentOutput() {
        return OUTPUTS[MathUtil.clamp(currentOutputIndex, 0, OUTPUTS.length - 1)] * MjAPI.MJ;
    }

    public boolean onWrenchInteract(Player player) {
        if (level == null || level.isClientSide()) return false;
        currentOutputIndex = (currentOutputIndex + 1) % OUTPUTS.length;
        buildcraft.lib.misc.MessageUtil.sendOverlayMessage(player,
            Component.translatable("chat.pipe.power.iron.mode", OUTPUTS[currentOutputIndex]));
        setChanged();

        BlockState state = getBlockState();
        level.sendBlockUpdated(getBlockPos(), state, state, 3);
        return true;
    }

    @Override
    public void getDebugInfo(java.util.List<String> left, java.util.List<String> right, Direction side) {
        super.getDebugInfo(left, right, side);
        left.add("Output = " + MjAPI.formatMj(getCurrentOutput()) + "/t");
    }

    @Override
    public void getClientDebugInfo(java.util.List<String> left, java.util.List<String> right, Direction side) {
        super.getClientDebugInfo(left, right, side);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("currentOutputIndex", currentOutputIndex);
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        currentOutputIndex = input.getIntOr("currentOutputIndex", 0);
        currentOutputIndex = MathUtil.clamp(currentOutputIndex, 0, OUTPUTS.length - 1);
    }
}

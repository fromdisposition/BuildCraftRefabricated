/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.behaviour;

import java.io.IOException;

import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.HitResult;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.transport.pipe.IFlowPowerLike;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeHolder.PipeMessageReceiver;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeApi.PowerTransferInfo;
import buildcraft.api.transport.pipe.PipeApi.RedstoneFluxTransferInfo;
import buildcraft.api.transport.pipe.PipeBehaviour;
import buildcraft.api.transport.pipe.PipeEventActionActivate;
import buildcraft.api.transport.pipe.PipeEventHandler;
import buildcraft.api.transport.pipe.PipeEventPower;
import buildcraft.api.transport.pipe.PipeEventRedstoneFlux;

import buildcraft.lib.misc.EntityUtil;
import buildcraft.lib.misc.MathUtil;

import buildcraft.transport.pipe.flow.PipeFlowRedstoneFlux;
import buildcraft.transport.statements.ActionPowerLimit;

public class PipeBehaviourLimiter extends PipeBehaviour {

    public static final int MAX_SHIFT = 6;

    private int limitShift = 0;

    public PipeBehaviourLimiter(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourLimiter(IPipe pipe, CompoundTag nbt) {
        super(pipe, nbt);
        limitShift = MathUtil.clamp(nbt.getIntOr("limitShift", 0), 0, MAX_SHIFT);
    }

    @Override
    public CompoundTag writeToNbt() {
        CompoundTag nbt = super.writeToNbt();
        nbt.putInt("limitShift", limitShift);
        return nbt;
    }

    @Override
    public void readFromNbt(CompoundTag nbt) {
        super.readFromNbt(nbt);
        limitShift = MathUtil.clamp(nbt.getIntOr("limitShift", 0), 0, MAX_SHIFT);
    }

    public void readPayload(FriendlyByteBuf buffer, Object side) throws IOException {
        limitShift = buffer.readUnsignedByte();
    }

    public void writePayload(FriendlyByteBuf buffer, Object side) {
        buffer.writeByte(limitShift);
    }

    @PipeEventHandler
    public void configurePower(PipeEventPower.Configure event) {
        if (limitShift == MAX_SHIFT) {
            event.disableTransfer();
        } else {
            event.setMaxPower(event.getMaxPower() >> limitShift);
        }
    }

    @PipeEventHandler
    public void configurePower(PipeEventRedstoneFlux.Configure event) {
        if (limitShift == MAX_SHIFT) {
            event.disableTransfer();
        } else {
            event.setMaxPower(event.getMaxPower() >> limitShift);
        }
    }

    @PipeEventHandler
    public void onActionActivate(PipeEventActionActivate event) {
        if (event.action instanceof ActionPowerLimit) {
            limitShift = ((ActionPowerLimit) event.action).limitShift;

            requestReconfigure();
        }
    }

    public boolean onPipeActivate(
        Player player, HitResult trace, float hitX, float hitY, float hitZ, EnumPipePart part
    ) {
        if (EntityUtil.getWrenchHand(player) == null) {
            return false;
        }

        if (!player.level().isClientSide()) {
            EntityUtil.activateWrench(player, trace);
            limitShift++;
            if (limitShift > MAX_SHIFT) {
                limitShift = 0;
            }

            boolean isRf = pipe.getFlow() instanceof PipeFlowRedstoneFlux;
            final int limit;
            if (limitShift == MAX_SHIFT) {
                limit = 0;
            } else if (isRf) {
                RedstoneFluxTransferInfo transferInfo = PipeApi.getRfTransferInfo(pipe.getDefinition());
                limit = transferInfo.transferPerTick >> limitShift;
            } else {
                PowerTransferInfo transferInfo = PipeApi.getPowerTransferInfo(pipe.getDefinition());
                limit = (int) ((transferInfo.transferPerTick >> limitShift) / MjAPI.MJ);
            }
            String key = "chat.pipe." + (isRf ? "rf" : "power") + ".iron.mode";
            buildcraft.lib.misc.MessageUtil.sendOverlayMessage(player,Component.translatable(key, limit));

            requestReconfigure();
        }
        return true;
    }

    private void requestReconfigure() {
        if (pipe.getFlow() instanceof IFlowPowerLike) {
            ((IFlowPowerLike) pipe.getFlow()).reconfigure();
            pipe.getHolder().scheduleNetworkUpdate(PipeMessageReceiver.BEHAVIOUR);
        }
    }

    public int getTextureIndex(Direction face) {
        return MAX_SHIFT - limitShift;
    }
}

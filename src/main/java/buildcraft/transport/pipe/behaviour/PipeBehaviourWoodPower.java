/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.behaviour;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.PipeBehaviour;

public class PipeBehaviourWoodPower extends PipeBehaviour {

    public PipeBehaviourWoodPower(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourWoodPower(IPipe pipe, CompoundTag nbt) {
        super(pipe, nbt);
    }

    public boolean canConnect(Direction face, PipeBehaviour other) {
        return !(other instanceof PipeBehaviourWoodPower);
    }

    public int getTextureIndex(Direction face) {
        if (face == null) {
            return 0;
        }
        if (pipe.getConnectedPipe(face) != null) {
            return 0;
        }
        BlockEntity tile = pipe.getConnectedTile(face);
        if (tile == null) {
            return 0;
        }
        if (pipe.getFlow() instanceof buildcraft.transport.pipe.flow.PipeFlowRedstoneFlux) {
            buildcraft.lib.transfer.energy.EnergyHandler handler = pipe.getHolder().getCapabilityFromPipe(face, buildcraft.lib.attachments.Attachments.Energy.BLOCK);
            if (handler == null) return 1;

            if (tile instanceof buildcraft.energy.tile.TileEngineRF) return 0;

            if (tile instanceof buildcraft.energy.tile.TileDynamoMJ) return 1;

            try (buildcraft.lib.transfer.transaction.Transaction tx = buildcraft.lib.transfer.transaction.Transaction.openRoot()) {
                if (handler.insert(1, tx) > 0) return 0;
            }
            return 1;
        } else {

            IMjReceiver recv = pipe.getHolder().getCapabilityFromPipe(face, MjAPI.CAP_RECEIVER);
            return recv == null ? 1 : recv.canReceive() ? 0 : 1;
        }
    }
}

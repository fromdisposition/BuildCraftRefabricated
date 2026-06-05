/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.plug;

import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import buildcraft.api.facades.IFacadePhasedState;
import buildcraft.api.facades.IFacadeState;

import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.net.PacketBufferBC;

public class FacadePhasedState implements IFacadePhasedState {
    public final FacadeBlockStateInfo stateInfo;

    @Nullable
    public final DyeColor activeColour;

    public FacadePhasedState(FacadeBlockStateInfo stateInfo, @Nullable DyeColor activeColour) {
        this.stateInfo = stateInfo;
        this.activeColour = activeColour;
    }

    public static FacadePhasedState readFromNbt(CompoundTag nbt) {
        FacadeBlockStateInfo stateInfo = FacadeStateManager.defaultState;
        if (nbt.contains("state")) {
            try {
                BlockState blockState = NbtUtils.readBlockState(
                    BuiltInRegistries.BLOCK, nbt.getCompoundOrEmpty("state"));
                stateInfo = FacadeStateManager.validFacadeStates.get(blockState);
                if (stateInfo == null) {
                    stateInfo = FacadeStateManager.defaultState;
                }
            } catch (Throwable t) {
                stateInfo = FacadeStateManager.defaultState;
            }
        }
        DyeColor colour = NBTUtilBC.readEnum(nbt.get("activeColour"), DyeColor.class);
        return new FacadePhasedState(stateInfo, colour);
    }

    public CompoundTag writeToNbt() {
        CompoundTag nbt = new CompoundTag();
        try {
            nbt.put("state", NbtUtils.writeBlockState(stateInfo.state));
        } catch (Throwable t) {
            throw new IllegalStateException("Writing facade block state"
                + "\n\tState = " + stateInfo
                + "\n\tBlock = " + stateInfo.state.getBlock() + "\n\tBlock Class = "
                + stateInfo.state.getBlock().getClass(), t);
        }
        if (activeColour != null) {
            nbt.put("activeColour", NBTUtilBC.writeEnum(activeColour));
        }
        return nbt;
    }

    public static FacadePhasedState readFromBuffer(PacketBufferBC buf) {
        int stateId = buf.readVarInt();
        BlockState state = Block.BLOCK_STATE_REGISTRY.byId(stateId);
        boolean hasColour = buf.readBoolean();
        DyeColor colour = hasColour ? buf.readEnumValue(DyeColor.class) : null;
        FacadeBlockStateInfo info = FacadeStateManager.validFacadeStates.get(state);
        if (info == null) {
            info = FacadeStateManager.defaultState;
        }
        return new FacadePhasedState(info, colour);
    }

    public void writeToBuffer(PacketBufferBC buf) {
        try {
            buf.writeVarInt(Block.BLOCK_STATE_REGISTRY.getId(stateInfo.state));
        } catch (Throwable t) {
            throw new IllegalStateException("Writing facade block state\n\tState = " + stateInfo.state, t);
        }
        buf.writeBoolean(activeColour != null);
        if (activeColour != null) {
            buf.writeEnumValue(activeColour);
        }
    }

    public FacadePhasedState withColour(DyeColor colour) {
        return new FacadePhasedState(stateInfo, colour);
    }

    public boolean isSideSolid(Direction side) {
        return stateInfo.isSideSolid[side.ordinal()];
    }

    @Override
    public String toString() {
        return (activeColour == null ? "" : activeColour + " ") + getState();
    }

    @Override
    public IFacadeState getState() {
        return stateInfo;
    }

    @Override
    public DyeColor getActiveColor() {
        return activeColour;
    }
}

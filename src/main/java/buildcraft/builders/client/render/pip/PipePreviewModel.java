/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.builders.client.render.pip;

import javax.annotation.Nullable;

import com.mojang.authlib.GameProfile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import buildcraft.api.transport.IWireManager;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pipe.PipeEvent;
import buildcraft.api.transport.pluggable.PipePluggable;

import buildcraft.transport.block.BlockPipeHolder;
import buildcraft.transport.client.model.key.PipeModelKey;
import buildcraft.transport.pipe.Pipe;

public final class PipePreviewModel {

    private PipePreviewModel() {}

    public static boolean isPipe(BlockState state) {
        return state != null && state.getBlock() instanceof BlockPipeHolder;
    }

    @Nullable
    public static PipeModelKey modelKey(@Nullable CompoundTag tileNbt) {
        if (tileNbt == null) {
            return null;
        }
        CompoundTag pipeNbt = tileNbt.getCompoundOrEmpty("pipe");
        if (pipeNbt.isEmpty()) {
            return null;
        }
        try {
            Pipe pipe = new Pipe(STUB_HOLDER, pipeNbt);
            return pipe.getModel();
        } catch (Throwable t) {

            return null;
        }
    }

    private static final IPipeHolder STUB_HOLDER = new IPipeHolder() {
        @Override
        public Level getPipeWorld() {
            return null;
        }

        @Override
        public BlockPos getPipePos() {
            return BlockPos.ZERO;
        }

        @Override
        public BlockEntity getPipeTile() {
            return null;
        }

        @Override
        public IPipe getPipe() {
            return null;
        }

        @Override
        public boolean canPlayerInteract(Player player) {
            return false;
        }

        @Override
        public PipePluggable getPluggable(Direction side) {
            return null;
        }

        @Override
        public BlockEntity getNeighbourTile(Direction side) {
            return null;
        }

        @Override
        public IPipe getNeighbourPipe(Direction side) {
            return null;
        }

        @Override
        public <T> T getCapabilityFromPipe(Direction side, Object capability) {
            return null;
        }

        @Override
        public IWireManager getWireManager() {
            return null;
        }

        @Override
        public GameProfile getOwner() {
            return null;
        }

        @Override
        public boolean fireEvent(PipeEvent event) {
            return false;
        }

        @Override
        public void scheduleRenderUpdate() {}

        @Override
        public void scheduleNetworkUpdate(PipeMessageReceiver... parts) {}

        @Override
        public void scheduleNetworkGuiUpdate(PipeMessageReceiver... parts) {}

        @Override
        public void sendMessage(PipeMessageReceiver to, IWriter writer) {}

        @Override
        public void sendGuiMessage(PipeMessageReceiver to, IWriter writer) {}

        @Override
        public void onPlayerOpen(Player player) {}

        @Override
        public void onPlayerClose(Player player) {}

        @Override
        public int getRedstoneInput(Direction side) {
            return 0;
        }

        @Override
        public boolean setRedstoneOutput(Direction side, int value) {
            return false;
        }
    };
}

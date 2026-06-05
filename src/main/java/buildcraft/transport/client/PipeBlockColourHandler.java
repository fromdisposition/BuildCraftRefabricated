/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;

import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import buildcraft.fabric.event.SubscribeEvent;
import buildcraft.fabric.client.event.RegisterColorHandlersEvent;

import buildcraft.api.transport.pluggable.PipePluggable;

import buildcraft.lib.misc.ColourUtil;
import buildcraft.transport.BCTransportBlocks;
import buildcraft.transport.tile.TilePipeHolder;

public class PipeBlockColourHandler {

    private static final int FACADE_TINT_BASE = 2;
    private static final int FACADE_TINT_MAX_DATA = 4;
    private static final int FACADE_TINT_LIST_SIZE =
            FACADE_TINT_BASE + FACADE_TINT_MAX_DATA * Direction.values().length;

    private static final BlockTintSource NO_TINT = state -> 0xFFFFFFFF;

    private static final BlockTintSource PIPE_COLOUR_TINT = new BlockTintSource() {
        @Override
        public int color(BlockState state) {
            return 0xFFFFFFFF;
        }

        @Override
        public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
            var be = level.getBlockEntity(pos);
            if (!(be instanceof TilePipeHolder tile)) return 0xFFFFFFFF;
            if (tile.getPipe() == null) return 0xFFFFFFFF;
            DyeColor colour = tile.getPipe().getModel().colour;
            if (colour == null) return 0xFFFFFFFF;
            return 0xFF000000 | ColourUtil.getLightHex(colour);
        }
    };

    private record FacadeTintSource(int wrappedTintIndex, Direction side) implements BlockTintSource {
        @Override
        public int color(BlockState state) {

            return -1;
        }

        @Override
        public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof TilePipeHolder tile)) return -1;
            PipePluggable plug = tile.getPluggable(side);
            Object wrapped = facadeWrappedState(plug);
            if (wrapped == null) return -1;
            BlockState wrappedState = (BlockState) wrapped;
            BlockTintSource wrappedSource =
                Minecraft.getInstance().getBlockColors().getTintSource(wrappedState, wrappedTintIndex);
            if (wrappedSource == null) return -1;
            return 0xFF000000 | wrappedSource.colorInWorld(wrappedState, level, pos);
        }
    }

    @SubscribeEvent
    public static void onRegisterBlockTintSources(RegisterColorHandlersEvent.BlockTintSources event) {
        List<BlockTintSource> sources = new ArrayList<>(FACADE_TINT_LIST_SIZE);
        sources.add(NO_TINT);
        sources.add(PIPE_COLOUR_TINT);
        for (int data = 0; data < FACADE_TINT_MAX_DATA; data++) {
            for (Direction side : Direction.values()) {
                sources.add(new FacadeTintSource(data, side));
            }
        }
        event.register(sources, BCTransportBlocks.PIPE_HOLDER.get());
    }

    @org.jspecify.annotations.Nullable
    private static Object facadeWrappedState(PipePluggable plug) {
        if (plug == null || !plug.getClass().getName().endsWith("PluggableFacade")) {
            return null;
        }
        try {
            Object states = plug.getClass().getField("states").get(plug);
            Object phasedStates = states.getClass().getField("phasedStates").get(states);
            Object[] arr = (Object[]) phasedStates;
            int activeState = plug.getClass().getField("activeState").getInt(plug);
            Object stateInfo = arr[activeState].getClass().getField("stateInfo").get(arr[activeState]);
            return stateInfo.getClass().getField("state").get(stateInfo);
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

}


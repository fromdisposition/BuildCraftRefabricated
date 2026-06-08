/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client;

import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.fabric.client.event.RegisterColorHandlersEvent;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.transport.BCTransportBlocks;
import buildcraft.transport.tile.TilePipeHolder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class PipeBlockColourHandler {
   private static final int FACADE_TINT_BASE = 2;
   private static final int FACADE_TINT_MAX_DATA = 4;
   private static final int FACADE_TINT_LIST_SIZE = 2 + 4 * Direction.values().length;
   private static final BlockTintSource NO_TINT = state -> -1;
   private static final BlockTintSource PIPE_COLOUR_TINT = new BlockTintSource() {
      public int color(BlockState state) {
         return -1;
      }

      public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
         if (level.getBlockEntity(pos) instanceof TilePipeHolder tile) {
            if (tile.getPipe() == null) {
               return -1;
            }

            DyeColor colour = tile.getPipe().getColour();
            return colour == null ? -1 : 0xFF000000 | ColourUtil.getLightHex(colour);
         } else {
            return -1;
         }
      }
   };

   public static void onRegisterBlockTintSources(RegisterColorHandlersEvent.BlockTintSources event) {
      List<BlockTintSource> sources = new ArrayList<>(FACADE_TINT_LIST_SIZE);
      sources.add(NO_TINT);
      sources.add(PIPE_COLOUR_TINT);

      for (int data = 0; data < 4; data++) {
         for (Direction side : Direction.values()) {
            sources.add(new PipeBlockColourHandler.FacadeTintSource(data, side));
         }
      }

      event.register(sources, BCTransportBlocks.PIPE_HOLDER);
   }

   private static @Nullable Object facadeWrappedState(PipePluggable plug) {
      if (plug != null && plug.getClass().getName().endsWith("PluggableFacade")) {
         try {
            Object states = plug.getClass().getField("states").get(plug);
            Object phasedStates = states.getClass().getField("phasedStates").get(states);
            Object[] arr = (Object[])phasedStates;
            int activeState = plug.getClass().getField("activeState").getInt(plug);
            Object stateInfo = arr[activeState].getClass().getField("stateInfo").get(arr[activeState]);
            return stateInfo.getClass().getField("state").get(stateInfo);
         } catch (ReflectiveOperationException e) {
            return null;
         }
      } else {
         return null;
      }
   }

   private record FacadeTintSource(int wrappedTintIndex, Direction side) implements BlockTintSource {
      public int color(BlockState state) {
         return -1;
      }

      public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
         if (level.getBlockEntity(pos) instanceof TilePipeHolder tile) {
            PipePluggable plug = tile.getPluggable(this.side);
            Object wrapped = PipeBlockColourHandler.facadeWrappedState(plug);
            if (wrapped == null) {
               return -1;
            }

            BlockState wrappedState = (BlockState)wrapped;
            BlockTintSource wrappedSource = Minecraft.getInstance().getBlockColors().getTintSource(wrappedState, this.wrappedTintIndex);
            return wrappedSource == null ? -1 : 0xFF000000 | wrappedSource.colorInWorld(wrappedState, level, pos);
         } else {
            return -1;
         }
      }
   }
}

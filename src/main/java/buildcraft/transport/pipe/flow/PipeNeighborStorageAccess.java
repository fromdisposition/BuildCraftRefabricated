/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.flow;

import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pipe.PipeFlow;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.transport.tile.TilePipeHolder;
import org.jspecify.annotations.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

public final class PipeNeighborStorageAccess {
   private PipeNeighborStorageAccess() {
   }

   @FunctionalInterface
   public interface PluggableLookup<T> {
      @Nullable T get(PipePluggable plug);
   }

   @FunctionalInterface
   public interface BlockLookup<T> {
      @Nullable T get(Level level, BlockPos pos, Direction side);
   }

   @FunctionalInterface
   public interface PipeFlowLookup<T> {
      @Nullable T get(PipeFlow flow, Direction querySide);
   }

   @Nullable
   public static <T> T storage(IPipeHolder holder, Direction from, PluggableLookup<T> pluggable, BlockLookup<T> block, PipeFlowLookup<T> pipeFlow) {
      if (holder != null && from != null && holder.getPipeWorld() != null) {
         if (holder.getPipeTile() instanceof TilePipeHolder tile) {
            PipePluggable plug = tile.getPluggable(from);
            if (plug != null) {
               T pluggableStorage = pluggable.get(plug);
               if (pluggableStorage != null) {
                  return pluggableStorage;
               }

               if (plug.isBlocking()) {
                  return null;
               }
            }
         }

         if (holder.getPipe() == null) {
            return null;
         }

         Level level = holder.getPipeWorld();
         BlockPos neighborPos = holder.getPipePos().relative(from);
         Direction querySide = from.getOpposite();
         T blockStorage = block.get(level, neighborPos, querySide);
         if (blockStorage != null) {
            return blockStorage;
         }

         IPipe neighborPipe = holder.getNeighbourPipe(from);
         return neighborPipe != null && neighborPipe.getFlow() != null ? pipeFlow.get(neighborPipe.getFlow(), querySide) : null;
      } else {
         return null;
      }
   }
}

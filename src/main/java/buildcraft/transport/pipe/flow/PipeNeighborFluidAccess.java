/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.flow;

import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.lib.fabric.transfer.BcTransfers;
import buildcraft.transport.pipe.PipePluggableTransferAccess;
import buildcraft.transport.tile.TilePipeHolder;
import javax.annotation.Nullable;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

public final class PipeNeighborFluidAccess {
   private PipeNeighborFluidAccess() {
   }

   @Nullable
   public static Storage<FluidVariant> storage(IPipeHolder holder, Direction from) {
      if (holder != null && from != null && holder.getPipeWorld() != null) {
         if (holder.getPipeTile() instanceof TilePipeHolder tile) {
            PipePluggable plug = tile.getPluggable(from);
            if (plug != null) {
               Storage<FluidVariant> pluggableStorage = PipePluggableTransferAccess.fluidStorage(plug);
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
         Storage<FluidVariant> blockStorage = BcTransfers.fluid(level, neighborPos, querySide);
         if (blockStorage != null) {
            return blockStorage;
         }

         IPipe neighborPipe = holder.getNeighbourPipe(from);
         return neighborPipe != null && neighborPipe.getFlow() != null ? PipeFlowInternalAccess.fluidStorage(neighborPipe.getFlow(), querySide) : null;
      } else {
         return null;
      }
   }

   public static boolean canConnect(IPipeHolder holder, Direction from) {
      return storage(holder, from) != null;
   }
}

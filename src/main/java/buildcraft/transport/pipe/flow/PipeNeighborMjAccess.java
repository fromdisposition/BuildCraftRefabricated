/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.flow;

import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjPassiveProvider;
import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.transport.tile.TilePipeHolder;
import org.jspecify.annotations.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

public final class PipeNeighborMjAccess {
   private PipeNeighborMjAccess() {
   }

   @Nullable
   public static IMjReceiver receiver(IPipeHolder holder, Direction from) {
      return lookup(holder, from, MjAPI.CAP_RECEIVER);
   }

   @Nullable
   public static IMjConnector connector(IPipeHolder holder, Direction from) {
      return lookup(holder, from, MjAPI.CAP_CONNECTOR);
   }

   @Nullable
   public static IMjPassiveProvider passiveProvider(IPipeHolder holder, Direction from) {
      return lookup(holder, from, MjAPI.CAP_PASSIVE_PROVIDER);
   }

   @Nullable
   @SuppressWarnings("unchecked")
   private static <T> T lookup(IPipeHolder holder, Direction from, Object capability) {
      if (holder != null && from != null && capability != null && holder.getPipeWorld() != null) {
         if (holder.getPipeTile() instanceof TilePipeHolder tile) {
            PipePluggable plug = tile.getPluggable(from);
            if (plug != null) {
               T internal = plug.getCapability(capability);
               if (internal != null) {
                  return internal;
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
         if (capability == MjAPI.CAP_RECEIVER) {
            T fromBlock = (T) buildcraft.lib.fabric.transfer.BcTransfers.mjReceiver(level, neighborPos, querySide);
            if (fromBlock != null) {
               return fromBlock;
            }
         } else if (capability == MjAPI.CAP_CONNECTOR) {
            T fromBlock = (T) MjAPI.CAP_CONNECTOR.find(level, neighborPos, null, null, querySide);
            if (fromBlock != null) {
               return fromBlock;
            }
         } else if (capability == MjAPI.CAP_PASSIVE_PROVIDER) {
            T fromBlock = (T) MjAPI.CAP_PASSIVE_PROVIDER.find(level, neighborPos, null, null, querySide);
            if (fromBlock != null) {
               return fromBlock;
            }
         }

         IPipe neighborPipe = holder.getNeighbourPipe(from);
         return neighborPipe != null && neighborPipe.getFlow() != null ? neighborPipe.getFlow().getCapability(capability, querySide) : null;
      } else {
         return null;
      }
   }
}

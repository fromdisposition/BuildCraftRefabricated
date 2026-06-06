package buildcraft.transport.pipe.flow;

import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.lib.fabric.transfer.BcTransfers;
import buildcraft.transport.pipe.PipePluggableTransferAccess;
import buildcraft.transport.tile.TilePipeHolder;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import team.reborn.energy.api.EnergyStorage;

public final class PipeNeighborEnergyAccess {
   private PipeNeighborEnergyAccess() {
   }

   @Nullable
   public static EnergyStorage storage(IPipeHolder holder, Direction from) {
      if (holder != null && from != null && holder.getPipeWorld() != null) {
         if (holder.getPipeTile() instanceof TilePipeHolder tile) {
            PipePluggable plug = tile.getPluggable(from);
            if (plug != null) {
               EnergyStorage pluggableStorage = PipePluggableTransferAccess.energyStorage(plug);
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
         EnergyStorage blockStorage = BcTransfers.energy(level, neighborPos, querySide);
         if (blockStorage != null) {
            return blockStorage;
         }

         IPipe neighborPipe = holder.getNeighbourPipe(from);
         return neighborPipe != null && neighborPipe.getFlow() != null ? PipeFlowInternalAccess.energyStorage(neighborPipe.getFlow(), querySide) : null;
      } else {
         return null;
      }
   }

   public static boolean canConnect(IPipeHolder holder, Direction from) {
      return storage(holder, from) != null;
   }
}

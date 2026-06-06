package buildcraft.transport.pipe.flow;

import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjPassiveProvider;
import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.lib.mj.MjBlockCapabilities;
import buildcraft.transport.tile.TilePipeHolder;
import javax.annotation.Nullable;
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
            T fromBlock = (T)MjBlockCapabilities.getReceiver(level, neighborPos, querySide);
            if (fromBlock != null) {
               return fromBlock;
            }
         } else if (capability == MjAPI.CAP_CONNECTOR) {
            T fromBlock = (T)MjBlockCapabilities.getConnector(level, neighborPos, querySide);
            if (fromBlock != null) {
               return fromBlock;
            }
         } else if (capability == MjAPI.CAP_PASSIVE_PROVIDER) {
            T fromBlock = (T)MjBlockCapabilities.getPassiveProvider(level, neighborPos, querySide);
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

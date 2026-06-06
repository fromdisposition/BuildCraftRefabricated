package buildcraft.transport.pipe;

import buildcraft.api.mj.MjAPI;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.transport.pipe.flow.PipeNeighborMjAccess;
import buildcraft.transport.tile.TilePipeHolder;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;

public final class PipeHolderCapabilityRouting {
   private PipeHolderCapabilityRouting() {
   }

   @Nullable
   public static <T> T resolve(TilePipeHolder holder, @Nullable Direction side, @Nonnull Object capability) {
      if (holder.getLevel() != null && side != null) {
         PipePluggable plug = holder.getPluggable(side);
         if (plug != null) {
            T internal = plug.getCapability(capability);
            if (internal != null) {
               return internal;
            }

            if (plug.isBlocking()) {
               return null;
            }
         }

         if (holder.getPipe() == null) {
            return null;
         }

         if (capability == MjAPI.CAP_RECEIVER) {
            return (T)PipeNeighborMjAccess.receiver(holder, side);
         }

         if (capability == MjAPI.CAP_CONNECTOR) {
            return (T)PipeNeighborMjAccess.connector(holder, side);
         }

         if (capability == MjAPI.CAP_PASSIVE_PROVIDER) {
            return (T)PipeNeighborMjAccess.passiveProvider(holder, side);
         }

         IPipe neighborPipe = holder.getNeighbourPipe(side);
         return neighborPipe != null && neighborPipe.getFlow() != null ? neighborPipe.getFlow().getCapability(capability, side.getOpposite()) : null;
      } else {
         return null;
      }
   }
}

package buildcraft.transport.tile;

import buildcraft.api.transport.pipe.IPipeHolder;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Consumer;

public final class PipeHolderNetworkFlush {
   private PipeHolderNetworkFlush() {
   }

   public static void flush(
      Set<IPipeHolder.PipeMessageReceiver> trackingUpdates,
      Set<IPipeHolder.PipeMessageReceiver> guiUpdates,
      Consumer<Set<IPipeHolder.PipeMessageReceiver>> sendTracking,
      Consumer<Set<IPipeHolder.PipeMessageReceiver>> sendGui
   ) {
      if (!trackingUpdates.isEmpty()) {
         sendTracking.accept(EnumSet.copyOf(trackingUpdates));
      }

      guiUpdates.removeAll(trackingUpdates);
      trackingUpdates.clear();
      if (!guiUpdates.isEmpty()) {
         sendGui.accept(EnumSet.copyOf(guiUpdates));
      }

      guiUpdates.clear();
   }
}

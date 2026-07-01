/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

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

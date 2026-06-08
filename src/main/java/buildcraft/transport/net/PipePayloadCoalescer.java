/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.net;

import buildcraft.lib.net.BCPacketLimits;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.core.BlockPos;

final class PipePayloadCoalescer {
   private final Map<BlockPos, Map<Integer, byte[]>> byPos = new HashMap<>();

   void put(MessagePipePayload payload) {
      this.byPos.computeIfAbsent(payload.pos(), unused -> new HashMap<>()).put(payload.receiverOrdinal(), payload.payload());
   }

   boolean isEmpty() {
      return this.byPos.isEmpty();
   }

   List<MessagePipePayload> drain() {
      List<MessagePipePayload> result = new ArrayList<>();
      int estimatedBytes = 0;
      Iterator<Entry<BlockPos, Map<Integer, byte[]>>> posIt = this.byPos.entrySet().iterator();

      while (posIt.hasNext()) {
         Entry<BlockPos, Map<Integer, byte[]>> posEntry = posIt.next();
         BlockPos pos = posEntry.getKey();
         Iterator<Entry<Integer, byte[]>> receiverIt = posEntry.getValue().entrySet().iterator();

         while (receiverIt.hasNext()) {
            Entry<Integer, byte[]> receiverEntry = receiverIt.next();
            byte[] data = receiverEntry.getValue();
            int entryBytes = BCPacketLimits.estimatePipePayloadEntryBytes(data);
            if (estimatedBytes + entryBytes > 524288 || result.size() >= 4000) {
               return result;
            }

            result.add(new MessagePipePayload(pos, receiverEntry.getKey(), data));
            estimatedBytes += entryBytes;
            receiverIt.remove();
         }

         if (posEntry.getValue().isEmpty()) {
            posIt.remove();
         }
      }

      return result;
   }
}

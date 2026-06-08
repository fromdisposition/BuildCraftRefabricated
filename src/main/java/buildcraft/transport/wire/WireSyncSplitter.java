/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.wire;

import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.network.FriendlyByteBuf;

public final class WireSyncSplitter {
   private WireSyncSplitter() {
   }

   public static List<PayloadWireSync> split(PayloadWireSync full) {
      List<PayloadWireSync> result = new ArrayList<>();
      if (full == null) {
         return result;
      }

      int[] removed = full.removedIds();
      boolean removedSent = false;
      if (full.topology() != null && !full.topology().isEmpty()) {
         Map<Integer, WireSystem> chunk = new HashMap<>();

         for (Entry<Integer, WireSystem> entry : full.topology().entrySet()) {
            chunk.put(entry.getKey(), entry.getValue());
            if (wouldExceed(PayloadWireSync.pack(chunk, null, null))) {
               chunk.remove(entry.getKey());
               if (!chunk.isEmpty()) {
                  result.add(PayloadWireSync.pack(chunk, null, removedSent ? null : removed));
                  removedSent = true;
               }

               chunk = new HashMap<>();
               chunk.put(entry.getKey(), entry.getValue());
            }
         }

         if (!chunk.isEmpty()) {
            result.add(PayloadWireSync.pack(chunk, null, removedSent ? null : removed));
            removedSent = true;
         }
      }

      if (full.powered() != null && !full.powered().isEmpty()) {
         Map<Integer, Boolean> chunk = new HashMap<>();

         for (Entry<Integer, Boolean> entry : full.powered().entrySet()) {
            chunk.put(entry.getKey(), entry.getValue());
            if (wouldExceed(PayloadWireSync.pack(null, chunk, null))) {
               chunk.remove(entry.getKey());
               if (!chunk.isEmpty()) {
                  result.add(PayloadWireSync.pack(null, chunk, null));
               }

               chunk = new HashMap<>();
               chunk.put(entry.getKey(), entry.getValue());
            }
         }

         if (!chunk.isEmpty()) {
            result.add(PayloadWireSync.pack(null, chunk, null));
         }
      }

      if (!removedSent && removed != null && removed.length > 0) {
         result.add(PayloadWireSync.pack(null, null, removed));
      }

      if (result.isEmpty()) {
         result.add(full);
      }

      return result;
   }

   private static boolean wouldExceed(PayloadWireSync msg) {
      FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
      PayloadWireSync.encodeTo(buf, msg);
      return buf.readableBytes() > 524288;
   }
}

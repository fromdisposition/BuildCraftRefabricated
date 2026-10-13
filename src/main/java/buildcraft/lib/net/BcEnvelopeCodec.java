/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.net;

import buildcraft.api.core.BCLog;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;

public final class BcEnvelopeCodec {
   private BcEnvelopeCodec() {
   }

   @Nullable
   public static byte[] encode(IPayloadWriter writer) {
      PacketBufferBC buffer = BcPayloadBuffers.create();

      try {
         writer.write(buffer);
         int size = buffer.readableBytes();
         if (size > 65536) {
            BCLog.logger.warn("[lib.net] Envelope payload exceeds limit ({} bytes)", size);
            return null;
         } else {
            byte[] data = new byte[size];
            buffer.readBytes(data);
            return data;
         }
      } finally {
         buffer.release();
      }
   }

   public static void decode(@Nullable byte[] payload, Consumer<FriendlyByteBuf> consumer) {
      if (payload != null) {
         PacketBufferBC buffer = BcPayloadBuffers.wrap(payload);

         try {
            consumer.accept(buffer);
         } finally {
            buffer.release();
         }
      }
   }
}

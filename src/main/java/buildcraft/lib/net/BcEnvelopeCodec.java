package buildcraft.lib.net;

import buildcraft.api.core.BCLog;
import io.netty.buffer.Unpooled;
import java.util.function.Consumer;
import javax.annotation.Nullable;

public final class BcEnvelopeCodec {
   private BcEnvelopeCodec() {
   }

   @Nullable
   public static byte[] encode(IPayloadWriter writer) {
      PacketBufferBC buffer = new PacketBufferBC(Unpooled.buffer());

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

   public static void decode(@Nullable byte[] payload, Consumer<PacketBufferBC> consumer) {
      if (payload != null) {
         PacketBufferBC buffer = new PacketBufferBC(Unpooled.wrappedBuffer(payload));

         try {
            consumer.accept(buffer);
         } finally {
            buffer.release();
         }
      }
   }
}

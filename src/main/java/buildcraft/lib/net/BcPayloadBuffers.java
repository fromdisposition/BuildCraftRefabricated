package buildcraft.lib.net;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;

/** Hub for BC bit-packed container/pipe payloads ({@link PacketBufferBC}). */
public final class BcPayloadBuffers {
   private BcPayloadBuffers() {
   }

   public static PacketBufferBC create() {
      return new PacketBufferBC(Unpooled.buffer());
   }

   public static PacketBufferBC wrap(byte[] payload) {
      return new PacketBufferBC(Unpooled.wrappedBuffer(payload));
   }

   public static PacketBufferBC ensure(ByteBuf buf) {
      return buf instanceof PacketBufferBC bc ? bc : new PacketBufferBC(buf);
   }

   public static PacketBufferBC ensure(FriendlyByteBuf buf) {
      return buf instanceof PacketBufferBC bc ? bc : new PacketBufferBC(buf);
   }
}

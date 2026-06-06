package buildcraft.transport.net;

import buildcraft.api.core.BCLog;
import buildcraft.fabric.network.BCPayloadContext;
import buildcraft.lib.net.BCPacketLimits;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;

public record MessageMultiPipePayload(List<MessagePipePayload> payloads) implements CustomPacketPayload {
   public static final Type<MessageMultiPipePayload> TYPE = new Type<>(Identifier.parse("buildcraftrefabricated:multi_pipe_payload"));
   public static final StreamCodec<RegistryFriendlyByteBuf, MessageMultiPipePayload> STREAM_CODEC = StreamCodec.of(
      MessageMultiPipePayload::encode, MessageMultiPipePayload::decode
   );

   private static void encode(RegistryFriendlyByteBuf buf, MessageMultiPipePayload msg) {
      int count = Math.min(msg.payloads.size(), 4000);
      if (msg.payloads.size() > count) {
         BCLog.logger.warn("[transport.net] Truncating multi_pipe_payload from {} to {} entries", msg.payloads.size(), count);
      }

      buf.writeVarInt(count);

      for (int i = 0; i < count; i++) {
         MessagePipePayload.STREAM_CODEC.encode(buf, msg.payloads.get(i));
      }
   }

   private static MessageMultiPipePayload decode(RegistryFriendlyByteBuf buf) {
      int count = BCPacketLimits.validateCount(buf.readVarInt(), 4000, "pipe payload entries");
      List<MessagePipePayload> payloads = new ArrayList<>(count);

      for (int i = 0; i < count; i++) {
         try {
            payloads.add((MessagePipePayload)MessagePipePayload.STREAM_CODEC.decode(buf));
         } catch (RuntimeException e) {
            if (e.getCause() instanceof IOException io) {
               throw new RuntimeException(io);
            }

            throw e;
         }
      }

      return new MessageMultiPipePayload(payloads);
   }

   public Type<MessageMultiPipePayload> type() {
      return TYPE;
   }

   public static void handle(MessageMultiPipePayload message, BCPayloadContext ctx) {
      for (MessagePipePayload payload : message.payloads) {
         MessagePipePayload.apply(payload, ctx);
      }
   }
}

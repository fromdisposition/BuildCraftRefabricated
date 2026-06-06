package buildcraft.transport.net;

import buildcraft.api.core.BCLog;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.fabric.network.BCPayloadContext;
import buildcraft.lib.net.BcEnvelopeCodec;
import buildcraft.lib.net.BoundedByteArrays;
import buildcraft.transport.pipe.Pipe;
import buildcraft.transport.tile.TilePipeHolder;
import java.io.IOException;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

public record MessagePipePayload(BlockPos pos, int receiverOrdinal, byte[] payload) implements CustomPacketPayload {
   public static final int LEGACY_WIRES_RECEIVER_ORDINAL = 8;
   public static final int MULTI_RECEIVER_ORDINAL = 9;
   public static final Type<MessagePipePayload> TYPE = new Type<>(Identifier.parse("buildcraftrefabricated:pipe_payload"));
   public static final StreamCodec<RegistryFriendlyByteBuf, MessagePipePayload> STREAM_CODEC = StreamCodec.of(
      MessagePipePayload::encode, MessagePipePayload::decode
   );

   private static void encode(RegistryFriendlyByteBuf buf, MessagePipePayload msg) {
      buf.writeBlockPos(msg.pos);
      buf.writeVarInt(msg.receiverOrdinal);
      BoundedByteArrays.write(buf, msg.payload);
   }

   private static MessagePipePayload decode(RegistryFriendlyByteBuf buf) {
      try {
         BlockPos pos = buf.readBlockPos();
         int receiverOrdinal = buf.readVarInt();
         byte[] payload = BoundedByteArrays.read(buf);
         return new MessagePipePayload(pos, receiverOrdinal, payload);
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   public Type<MessagePipePayload> type() {
      return TYPE;
   }

   public static void handle(MessagePipePayload message, BCPayloadContext ctx) {
      apply(message, ctx);
   }

   public static void apply(MessagePipePayload message, BCPayloadContext ctx) {
      Level world = ctx.player().level();
      if (world != null) {
         if (world.getBlockEntity(message.pos) instanceof TilePipeHolder holder) {
            Pipe pipe = holder.getPipe();
            if (pipe != null) {
               try {
                  BcEnvelopeCodec.decode(message.payload, buffer -> {
                     try {
                        if (message.receiverOrdinal == LEGACY_WIRES_RECEIVER_ORDINAL) {
                           PipeReceiverPayloadCodec.ignoreLegacyWiresPayload(holder, buffer);
                        } else if (message.receiverOrdinal == MULTI_RECEIVER_ORDINAL) {
                           PipeReceiverPayloadCodec.readMulti(holder, pipe, buffer);
                        } else {
                           IPipeHolder.PipeMessageReceiver[] receivers = IPipeHolder.PipeMessageReceiver.VALUES;
                           if (message.receiverOrdinal < 0 || message.receiverOrdinal >= receivers.length) {
                              BCLog.logger.warn("[transport.net] Invalid pipe message receiver ordinal: " + message.receiverOrdinal);
                              return;
                           }

                           PipeReceiverPayloadCodec.read(receivers[message.receiverOrdinal], holder, pipe, buffer);
                        }
                     } catch (IOException e) {
                        throw new RuntimeException(e);
                     }
                  });
               } catch (Exception e) {
                  BCLog.logger.warn("[transport.net] Error handling pipe payload at " + message.pos, e);
               }
            }
         }
      }
   }
}

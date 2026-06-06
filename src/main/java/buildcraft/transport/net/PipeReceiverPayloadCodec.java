package buildcraft.transport.net;

import buildcraft.api.core.BCLog;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pipe.PipeFlow;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.transport.pipe.Pipe;
import buildcraft.transport.tile.TilePipeHolder;
import java.io.IOException;
import java.util.Set;
import net.minecraft.network.FriendlyByteBuf;

public final class PipeReceiverPayloadCodec {
   private PipeReceiverPayloadCodec() {
   }

   public static boolean isActiveReceiver(IPipeHolder.PipeMessageReceiver receiver) {
      return receiver != IPipeHolder.PipeMessageReceiver.WIRES;
   }

   public static void write(IPipeHolder.PipeMessageReceiver receiver, TilePipeHolder holder, FriendlyByteBuf buffer) {
      switch (receiver) {
         case BEHAVIOUR:
            Pipe pipe = holder.getPipe();
            if (pipe == null) {
               buffer.writeBoolean(false);
            } else {
               buffer.writeBoolean(true);
               pipe.writePayload(buffer);
            }
            break;
         case FLOW:
            Pipe pipex = holder.getPipe();
            if (pipex != null && pipex.getFlow() != null) {
               buffer.writeBoolean(true);
               buffer.writeShort(1);
               pipex.getFlow().writePayload(1, buffer, null);
            } else {
               buffer.writeBoolean(false);
            }
            break;
         default:
            if (receiver.face != null) {
               PipePluggable plug = holder.getPluggable(receiver.face);
               if (plug != null) {
                  plug.writePayload(buffer, receiver.face);
               }
            }
      }
   }

   public static void read(IPipeHolder.PipeMessageReceiver receiver, TilePipeHolder holder, Pipe pipe, PacketBufferBC buffer) throws IOException {
      switch (receiver) {
         case BEHAVIOUR:
            if (buffer.readBoolean()) {
               pipe.readPayload(buffer);
            }
            break;
         case FLOW:
            if (!buffer.readBoolean()) {
               return;
            }

            PipeFlow flow = pipe.getFlow();
            if (flow != null) {
               int id = buffer.readShort();
               flow.readPayload(id, buffer, null);
            }
            break;
         default:
            if (receiver == IPipeHolder.PipeMessageReceiver.WIRES) {
               BCLog.logger.warn("[transport.net] Ignoring deprecated WIRES pipe payload at {}", holder.getPipePos());
               return;
            }

            if (receiver.face != null) {
               PipePluggable plug = holder.getPluggable(receiver.face);
               if (plug != null) {
                  plug.readPayload(buffer, receiver.face, Boolean.TRUE);
               }
            }
      }
   }

   public static int buildMask(Set<IPipeHolder.PipeMessageReceiver> parts) {
      int mask = 0;

      for (IPipeHolder.PipeMessageReceiver part : parts) {
         if (isActiveReceiver(part)) {
            mask |= 1 << part.ordinal();
         }
      }

      return mask;
   }

   public static void writeMulti(Set<IPipeHolder.PipeMessageReceiver> parts, TilePipeHolder holder, FriendlyByteBuf buffer) {
      int mask = buildMask(parts);
      buffer.writeShort(mask);

      for (IPipeHolder.PipeMessageReceiver receiver : IPipeHolder.PipeMessageReceiver.VALUES) {
         if (isActiveReceiver(receiver) && (mask & 1 << receiver.ordinal()) != 0) {
            write(receiver, holder, buffer);
         }
      }
   }

   public static void readMulti(TilePipeHolder holder, Pipe pipe, PacketBufferBC buffer) throws IOException {
      int mask = buffer.readUnsignedShort();

      for (IPipeHolder.PipeMessageReceiver receiver : IPipeHolder.PipeMessageReceiver.VALUES) {
         if (isActiveReceiver(receiver) && (mask & 1 << receiver.ordinal()) != 0) {
            read(receiver, holder, pipe, buffer);
         }
      }
   }
}

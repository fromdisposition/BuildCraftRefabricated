/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.net;

import buildcraft.api.core.BCLog;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pipe.PipeFlow;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.lib.net.BcPayloadBuffers;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.transport.pipe.Pipe;
import buildcraft.transport.tile.TilePipeHolder;
import java.util.Set;
import net.minecraft.network.FriendlyByteBuf;

public final class PipeReceiverPayloadCodec {
   private PipeReceiverPayloadCodec() {
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
               pipex.getFlow().writePayload(1, buffer);
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

   public static void read(IPipeHolder.PipeMessageReceiver receiver, TilePipeHolder holder, Pipe pipe, FriendlyByteBuf buffer) {
      switch (receiver) {
         case BEHAVIOUR:
            if (buffer.readBoolean()) {
               if (pipe != null) {
                  pipe.readPayload(buffer);
               } else {
                  BCLog.logger.warn("[transport.net] Unexpected pipe behaviour payload without pipe at {}", holder.getPipePos());
               }
            }
            break;
         case FLOW:
            if (!buffer.readBoolean()) {
               return;
            }

            if (pipe == null) {
               BCLog.logger.warn("[transport.net] Unexpected pipe flow payload without pipe at {}", holder.getPipePos());
               return;
            }

            PipeFlow flow = pipe.getFlow();
            if (flow != null) {
               int id = buffer.readShort();
               flow.readPayload(id, buffer);
            }
            break;
         default:
            if (receiver.face != null) {
               PipePluggable plug = holder.getPluggable(receiver.face);
               if (plug != null) {
                  plug.readPayload(buffer, receiver.face, true);
               }
            }
      }
   }

   public static int buildMask(Set<IPipeHolder.PipeMessageReceiver> parts) {
      int mask = 0;

      for (IPipeHolder.PipeMessageReceiver part : parts) {
         mask |= 1 << part.ordinal();
      }

      return mask;
   }

   /**
    * Each receiver's payload is length-prefixed and read from an isolated slice. The write and read sides of a
    * segment can legitimately fall out of step for a tick — a pluggable can exist on only one side during
    * place/remove races, and gate payloads bail out early on unknown statement tags — and without the frame any
    * such mismatch silently shifted every byte of the receivers after it in the same packet. With it the damage
    * is contained to (and logged for) the one segment.
    */
   public static void writeMulti(Set<IPipeHolder.PipeMessageReceiver> parts, TilePipeHolder holder, FriendlyByteBuf buffer) {
      int mask = buildMask(parts);
      buffer.writeShort(mask);

      for (IPipeHolder.PipeMessageReceiver receiver : IPipeHolder.PipeMessageReceiver.VALUES) {
         if ((mask & 1 << receiver.ordinal()) != 0) {
            // Each segment gets its own PacketBufferBC: receivers write bit-packed booleans, so a segment must
            // both start with a fresh bit cache (no partial byte shared with the previous segment) and be read
            // back through the same buffer class — a plain slice reader would consume whole bytes per boolean.
            PacketBufferBC segment = BcPayloadBuffers.create();

            try {
               write(receiver, holder, segment);
               buffer.writeInt(segment.readableBytes());
               buffer.writeBytes(segment);
            } finally {
               segment.release();
            }
         }
      }
   }

   public static void readMulti(TilePipeHolder holder, Pipe pipe, FriendlyByteBuf buffer) {
      int mask = buffer.readUnsignedShort();

      for (IPipeHolder.PipeMessageReceiver receiver : IPipeHolder.PipeMessageReceiver.VALUES) {
         if ((mask & 1 << receiver.ordinal()) != 0) {
            int length = buffer.readInt();
            if (length < 0 || length > buffer.readableBytes()) {
               BCLog.logger.warn("[transport.net] Corrupt pipe payload segment {} at {} (length {})", receiver, holder.getPipePos(), length);
               return;
            }

            PacketBufferBC segment = BcPayloadBuffers.ensure(buffer.readSlice(length));

            try {
               read(receiver, holder, pipe, segment);
            } catch (Exception e) {
               BCLog.logger.warn("[transport.net] Failed to read pipe payload segment {} at {}", receiver, holder.getPipePos(), e);
            }
         }
      }
   }

   public static void ignoreLegacyWiresPayload(TilePipeHolder holder, FriendlyByteBuf buffer) {
      BCLog.logger.warn("[transport.net] Ignoring deprecated WIRES pipe payload at {}", holder.getPipePos());
   }
}

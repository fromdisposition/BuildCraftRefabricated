/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.net;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;

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

   public static void writeEnum(FriendlyByteBuf buf, Enum<?> value) {
      buf.writeVarInt(value.ordinal());
   }

   public static <E extends Enum<E>> E readEnum(FriendlyByteBuf buf, Class<E> type) {
      E[] values = type.getEnumConstants();
      int ordinal = buf.readVarInt();
      if (ordinal < 0 || ordinal >= values.length) {
         throw new DecoderException("Enum ordinal " + ordinal + " out of range for " + type.getSimpleName());
      }

      return values[ordinal];
   }
}

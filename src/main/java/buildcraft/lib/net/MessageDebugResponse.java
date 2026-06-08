/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.net;

import buildcraft.fabric.network.BCPayloadContext;
import buildcraft.lib.debug.ClientDebuggables;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;

public record MessageDebugResponse(List<String> left, List<String> right) implements CustomPacketPayload {
   public static final Type<MessageDebugResponse> TYPE = new Type<>(Identifier.parse("buildcraftrefabricated:debug_response"));
   public static final StreamCodec<RegistryFriendlyByteBuf, MessageDebugResponse> STREAM_CODEC = StreamCodec.of(
      MessageDebugResponse::encode, MessageDebugResponse::decode
   );

   private static void encode(RegistryFriendlyByteBuf buf, MessageDebugResponse msg) {
      buf.writeVarInt(msg.left.size());

      for (String s : msg.left) {
         buf.writeUtf(s);
      }

      buf.writeVarInt(msg.right.size());

      for (String s : msg.right) {
         buf.writeUtf(s);
      }
   }

   private static MessageDebugResponse decode(RegistryFriendlyByteBuf buf) {
      int leftCount = BCPacketLimits.validateCount(buf.readVarInt(), 256, "debug left");
      List<String> left = new ArrayList<>(leftCount);

      for (int i = 0; i < leftCount; i++) {
         left.add(buf.readUtf(4096));
      }

      int rightCount = BCPacketLimits.validateCount(buf.readVarInt(), 256, "debug right");
      List<String> right = new ArrayList<>(rightCount);

      for (int i = 0; i < rightCount; i++) {
         right.add(buf.readUtf(4096));
      }

      return new MessageDebugResponse(left, right);
   }

   public Type<MessageDebugResponse> type() {
      return TYPE;
   }

   public static void handle(MessageDebugResponse message, BCPayloadContext ctx) {
      ClientDebuggables.SERVER_LEFT.clear();
      ClientDebuggables.SERVER_LEFT.addAll(message.left);
      ClientDebuggables.SERVER_RIGHT.clear();
      ClientDebuggables.SERVER_RIGHT.addAll(message.right);
   }
}

/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.net;

import buildcraft.api.core.BCLog;
import buildcraft.fabric.network.BCPayloadContext;
import buildcraft.lib.gui.IBcMenu;
import java.io.IOException;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public record MessageContainerPayload(int containerId, int messageId, byte[] payload) implements CustomPacketPayload {
   public static final Type<MessageContainerPayload> TYPE = new Type<>(Identifier.parse("buildcraftrefabricated:container"));
   public static final StreamCodec<RegistryFriendlyByteBuf, MessageContainerPayload> STREAM_CODEC = StreamCodec.of(
      MessageContainerPayload::encode, MessageContainerPayload::decode
   );

   private static void encode(RegistryFriendlyByteBuf buf, MessageContainerPayload msg) {
      buf.writeVarInt(msg.containerId);
      buf.writeVarInt(msg.messageId);
      BoundedByteArrays.write(buf, msg.payload);
   }

   private static MessageContainerPayload decode(RegistryFriendlyByteBuf buf) {
      try {
         int containerId = buf.readVarInt();
         int messageId = buf.readVarInt();
         byte[] payload = BoundedByteArrays.read(buf);
         return new MessageContainerPayload(containerId, messageId, payload);
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   public Type<MessageContainerPayload> type() {
      return TYPE;
   }

   public static void handle(MessageContainerPayload message, BCPayloadContext ctx) {
      Player player = ctx.player();
      AbstractContainerMenu openContainer = player.containerMenu;
      if (openContainer == null) {
         BCLog.logger.warn("[lib.net] Received container message but player has no open container");
      } else if (openContainer.containerId == message.containerId) {
         if (openContainer instanceof IBcMenu bcContainer) {
            boolean isClient = player.level().isClientSide();
            if (message.payload != null) {
               if (!isClient && !bcContainer.stillValid(player)) {
                  BCLog.logger.warn("[lib.net] Received container message but container is no longer valid for player");
               } else {
                  try {
                     BcEnvelopeCodec.decode(message.payload, buffer -> bcContainer.readMessage(message.messageId, buffer, isClient, ctx));
                  } catch (Exception e) {
                     BCLog.logger.warn("[lib.net] Error handling container message (id=" + message.messageId + ")", e);
                  }
               }
            }
         } else {
            BCLog.logger.warn("[lib.net] Received container message but open container is not a BcMenu (got " + openContainer.getClass().getName() + ")");
         }
      }
   }
}

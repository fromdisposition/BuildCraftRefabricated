/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.lib.net;

import io.netty.buffer.Unpooled;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

import buildcraft.fabric.network.BCPayloadContext;

import buildcraft.api.core.BCLog;
import buildcraft.lib.gui.ContainerBC_Neptune;

public record MessageContainerPayload(
        int containerId,
        int messageId,
        byte[] payload
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<MessageContainerPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.parse("buildcraftrefabricated:container"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MessageContainerPayload> STREAM_CODEC =
            StreamCodec.of(MessageContainerPayload::encode, MessageContainerPayload::decode);

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
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Type<MessageContainerPayload> type() {
        return TYPE;
    }

    public static void handle(MessageContainerPayload message, BCPayloadContext ctx) {
        Player player = ctx.player();
        AbstractContainerMenu openContainer = player.containerMenu;
        if (openContainer == null) {
            BCLog.logger.warn("[lib.net] Received container message but player has no open container");
            return;
        }
        if (openContainer.containerId != message.containerId) {
            return;
        }
        if (!(openContainer instanceof ContainerBC_Neptune bcContainer)) {
            BCLog.logger.warn("[lib.net] Received container message but open container is not a ContainerBC_Neptune"
                + " (got " + openContainer.getClass().getName() + ")");
            return;
        }

        boolean isClient = player.level().isClientSide();
        if (message.payload == null) {
            return;
        }

        if (!isClient && !bcContainer.stillValid(player)) {
            BCLog.logger.warn("[lib.net] Received container message but container is no longer valid for player");
            return;
        }

        PacketBufferBC buffer = new PacketBufferBC(Unpooled.wrappedBuffer(message.payload));
        try {
            bcContainer.readMessage(message.messageId, buffer, isClient, ctx);
        } catch (Exception e) {
            BCLog.logger.warn("[lib.net] Error handling container message (id=" + message.messageId + ")", e);
        } finally {
            buffer.release();
        }
    }
}

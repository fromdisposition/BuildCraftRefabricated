/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.lib.net;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import buildcraft.lib.debug.ClientDebuggables;
import buildcraft.lib.net.BCPacketLimits;

public record MessageDebugResponse(
        List<String> left,
        List<String> right
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<MessageDebugResponse> TYPE =
            new CustomPacketPayload.Type<>(Identifier.parse("buildcraftrefabricated:debug_response"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MessageDebugResponse> STREAM_CODEC =
            StreamCodec.of(MessageDebugResponse::encode, MessageDebugResponse::decode);

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
        int leftCount = BCPacketLimits.validateCount(buf.readVarInt(), BCPacketLimits.MAX_DEBUG_STRINGS, "debug left");
        List<String> left = new ArrayList<>(leftCount);
        for (int i = 0; i < leftCount; i++) {
            left.add(buf.readUtf(BCPacketLimits.MAX_DEBUG_STRING_LENGTH));
        }
        int rightCount = BCPacketLimits.validateCount(buf.readVarInt(), BCPacketLimits.MAX_DEBUG_STRINGS, "debug right");
        List<String> right = new ArrayList<>(rightCount);
        for (int i = 0; i < rightCount; i++) {
            right.add(buf.readUtf(BCPacketLimits.MAX_DEBUG_STRING_LENGTH));
        }
        return new MessageDebugResponse(left, right);
    }

    @Override
    public Type<MessageDebugResponse> type() {
        return TYPE;
    }

    public static void handle(MessageDebugResponse message, buildcraft.fabric.network.BCPayloadContext ctx) {
        ClientDebuggables.SERVER_LEFT.clear();
        ClientDebuggables.SERVER_LEFT.addAll(message.left);
        ClientDebuggables.SERVER_RIGHT.clear();
        ClientDebuggables.SERVER_RIGHT.addAll(message.right);
    }
}

/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.lib.net;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import buildcraft.api.core.BCLog;
import buildcraft.lib.marker.MarkerCache;
import buildcraft.lib.net.BCPacketLimits;

public record MessageMarker(
        boolean add,
        boolean connection,
        int cacheId,
        List<BlockPos> positions
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<MessageMarker> TYPE =
            new CustomPacketPayload.Type<>(Identifier.parse("buildcraftrefabricated:marker"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MessageMarker> STREAM_CODEC =
            StreamCodec.of(MessageMarker::encode, MessageMarker::decode);

    private static void encode(RegistryFriendlyByteBuf buf, MessageMarker msg) {
        buf.writeBoolean(msg.add);
        buf.writeBoolean(msg.connection);
        buf.writeShort(msg.cacheId);
        buf.writeShort(msg.positions.size());
        for (BlockPos pos : msg.positions) {
            buf.writeBlockPos(pos);
        }
    }

    private static MessageMarker decode(RegistryFriendlyByteBuf buf) {
        boolean add = buf.readBoolean();
        boolean connection = buf.readBoolean();
        int cacheId = buf.readShort();
        int count = BCPacketLimits.validateCount(buf.readShort(), BCPacketLimits.MAX_MARKER_POSITIONS, "marker positions");
        List<BlockPos> positions = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            positions.add(buf.readBlockPos());
        }
        return new MessageMarker(add, connection, cacheId, positions);
    }

    @Override
    public Type<MessageMarker> type() {
        return TYPE;
    }

    public static void handle(MessageMarker message, buildcraft.fabric.network.BCPayloadContext ctx) {
        if (message.cacheId < 0 || message.cacheId >= MarkerCache.CACHES.size()) {
            BCLog.logger.warn("[lib.messages][marker] Invalid cache ID: " + message.cacheId);
            return;
        }
        net.minecraft.world.level.Level world = ctx.player().level();
        MarkerCache<?> cache = MarkerCache.CACHES.get(message.cacheId);
        cache.getSubCache(world).handleMessageMain(message);
    }
}

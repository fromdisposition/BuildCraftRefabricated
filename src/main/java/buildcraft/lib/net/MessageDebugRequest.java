/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.lib.net;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

import buildcraft.api.tiles.IDebuggable;

import buildcraft.lib.item.ItemDebugger;

public record MessageDebugRequest(
        BlockPos pos,
        Direction side
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<MessageDebugRequest> TYPE =
            new CustomPacketPayload.Type<>(Identifier.parse("buildcraftrefabricated:debug_request"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MessageDebugRequest> STREAM_CODEC =
            StreamCodec.of(MessageDebugRequest::encode, MessageDebugRequest::decode);

    private static void encode(RegistryFriendlyByteBuf buf, MessageDebugRequest msg) {
        buf.writeBlockPos(msg.pos);
        buf.writeEnum(msg.side);
    }

    private static MessageDebugRequest decode(RegistryFriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        Direction side = buf.readEnum(Direction.class);
        return new MessageDebugRequest(pos, side);
    }

    @Override
    public Type<MessageDebugRequest> type() {
        return TYPE;
    }

    public static void handle(MessageDebugRequest message, buildcraft.fabric.network.BCPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer player)) {
            return;
        }
        if (!ItemDebugger.isShowDebugInfo(player)) {
            buildcraft.lib.fabric.PacketDistributor.sendToPlayer(player, new MessageDebugResponse(List.of(), List.of()));
            return;
        }
        if (player.distanceToSqr(message.pos.getX() + 0.5, message.pos.getY() + 0.5, message.pos.getZ() + 0.5) > 64.0) {
            return;
        }
        BlockEntity tile = player.level().getBlockEntity(message.pos);
        if (tile instanceof IDebuggable debuggable) {
            List<String> left = new ArrayList<>();
            List<String> right = new ArrayList<>();
            debuggable.getDebugInfo(left, right, message.side);
            buildcraft.lib.fabric.PacketDistributor.sendToPlayer(player, new MessageDebugResponse(left, right));
        }
    }
}

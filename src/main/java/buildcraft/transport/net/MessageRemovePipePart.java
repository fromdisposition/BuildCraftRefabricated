/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.net;

import buildcraft.fabric.network.BCPayloadContext;
import buildcraft.transport.block.BlockPipeHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

/**
 * Serverbound: the client left-clicked a pluggable/wire on the pipe at {@code pos} and wants just that part removed.
 * Sent instead of the vanilla break so the client never starts breaking the whole pipe (no break-crack flicker).
 * The server is authoritative: it re-raytraces the player's own look in {@link BlockPipeHolder#removeHitPart} and
 * removes only the part actually under the crosshair, so a spoofed {@code pos} can at most remove a part the player
 * is genuinely looking at within reach.
 */
public record MessageRemovePipePart(BlockPos pos) implements CustomPacketPayload {
   public static final Type<MessageRemovePipePart> TYPE = new Type<>(Identifier.parse("buildcraftrefabricated:remove_pipe_part"));
   public static final StreamCodec<RegistryFriendlyByteBuf, MessageRemovePipePart> STREAM_CODEC = StreamCodec.of(
      MessageRemovePipePart::encode, MessageRemovePipePart::decode
   );

   private static void encode(RegistryFriendlyByteBuf buf, MessageRemovePipePart msg) {
      buf.writeBlockPos(msg.pos);
   }

   private static MessageRemovePipePart decode(RegistryFriendlyByteBuf buf) {
      return new MessageRemovePipePart(buf.readBlockPos());
   }

   public Type<MessageRemovePipePart> type() {
      return TYPE;
   }

   public static void handle(MessageRemovePipePart message, BCPayloadContext ctx) {
      if (ctx.player() instanceof ServerPlayer player
         && player.distanceToSqr(message.pos.getX() + 0.5, message.pos.getY() + 0.5, message.pos.getZ() + 0.5) <= 64.0
         && player.level().getBlockState(message.pos).getBlock() instanceof BlockPipeHolder) {
         BlockPipeHolder.removeHitPart(player.level(), message.pos, player);
      }
   }
}

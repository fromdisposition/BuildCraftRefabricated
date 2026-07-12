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
import net.minecraft.util.Mth;

/**
 * Serverbound: the client left-clicked a pluggable/wire on the pipe at {@code pos} and wants just that part removed.
 * Sent instead of the vanilla break so the client never starts breaking the whole pipe (no break-crack flicker).
 *
 * <p>Carries the client's exact in-block hit point (like vanilla's own dig packet carries pos+face) instead of
 * re-raytracing on the server: the server-side player position lags the client's (worst while flying/looking down),
 * so a server re-pick could resolve a DIFFERENT part than the one under the client's crosshair -- removing a gate on
 * a side face worked, but aiming down at a gate on top removed the wrong thing. The server still validates: the
 * player must be within reach of the pipe, the hit point is clamped inside the block, and only a part that actually
 * exists at that point can be removed -- so a spoofed packet can at most remove a pluggable within normal reach.
 */
public record MessageRemovePipePart(BlockPos pos, float lx, float ly, float lz) implements CustomPacketPayload {
   public static final Type<MessageRemovePipePart> TYPE = new Type<>(Identifier.parse("buildcraftrefabricated:remove_pipe_part"));
   public static final StreamCodec<RegistryFriendlyByteBuf, MessageRemovePipePart> STREAM_CODEC = StreamCodec.of(
      MessageRemovePipePart::encode, MessageRemovePipePart::decode
   );

   private static void encode(RegistryFriendlyByteBuf buf, MessageRemovePipePart msg) {
      buf.writeBlockPos(msg.pos);
      buf.writeFloat(msg.lx);
      buf.writeFloat(msg.ly);
      buf.writeFloat(msg.lz);
   }

   private static MessageRemovePipePart decode(RegistryFriendlyByteBuf buf) {
      BlockPos pos = buf.readBlockPos();
      float lx = Mth.clamp(buf.readFloat(), 0.0F, 1.0F);
      float ly = Mth.clamp(buf.readFloat(), 0.0F, 1.0F);
      float lz = Mth.clamp(buf.readFloat(), 0.0F, 1.0F);
      return new MessageRemovePipePart(pos, lx, ly, lz);
   }

   public Type<MessageRemovePipePart> type() {
      return TYPE;
   }

   public static void handle(MessageRemovePipePart message, BCPayloadContext ctx) {
      if (ctx.player() instanceof ServerPlayer player
         && player.distanceToSqr(message.pos.getX() + 0.5, message.pos.getY() + 0.5, message.pos.getZ() + 0.5) <= 36.0
         && player.level().getBlockState(message.pos).getBlock() instanceof BlockPipeHolder) {
         BlockPipeHolder.removeHitPart(player.level(), message.pos, player, message.lx, message.ly, message.lz);
      }
   }
}

/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.net;

import buildcraft.fabric.network.BCPayloadContext;
import buildcraft.lib.misc.BreakEventCompat;
import buildcraft.transport.block.BlockPipeHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Carries the client's exact hit point instead of a server re-raytrace, since the server-side player position lags
 * the client's and could otherwise resolve a different part. Server still bounds it to reach range and an existing part.
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

   @Override
   public Type<MessageRemovePipePart> type() {
      return TYPE;
   }

   public static void handle(MessageRemovePipePart message, BCPayloadContext ctx) {
      if (!(ctx.player() instanceof ServerPlayer player) || !(player.level() instanceof ServerLevel level)) {
         return;
      }

      if (player.isSpectator() || !player.getAbilities().mayBuild) {
         return;
      }

      if (player.distanceToSqr(message.pos.getX() + 0.5, message.pos.getY() + 0.5, message.pos.getZ() + 0.5) > 36.0
         || !level.getChunkSource().hasChunk(message.pos.getX() >> 4, message.pos.getZ() >> 4)) {
         return;
      }

      BlockState state = level.getBlockState(message.pos);
      if (!(state.getBlock() instanceof BlockPipeHolder)) {
         return;
      }

      if (!level.mayInteract(player, message.pos) || !BreakEventCompat.canBreak(level, message.pos, state, player)) {
         return;
      }

      BlockPipeHolder.removeHitPart(level, message.pos, player, message.lx, message.ly, message.lz);
   }
}

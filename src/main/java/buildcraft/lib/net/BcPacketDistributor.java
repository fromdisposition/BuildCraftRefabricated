/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.net;

import buildcraft.fabric.network.PacketDistributor;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

public final class BcPacketDistributor {
   private BcPacketDistributor() {
   }

   public static void sendToServer(CustomPacketPayload payload) {
      PacketDistributor.sendToServer(payload);
   }

   public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
      PacketDistributor.sendToPlayer(player, payload);
   }

   public static void sendToPlayersInDimension(ServerLevel level, CustomPacketPayload payload) {
      PacketDistributor.sendToPlayersInDimension(level, payload);
   }

   public static void sendToPlayersTrackingChunk(ServerLevel level, ChunkPos chunkPos, CustomPacketPayload payload) {
      PacketDistributor.sendToPlayersTrackingChunk(level, chunkPos, payload);
   }

   public static void sendToPlayersTracking(ServerLevel level, BlockPos pos, CustomPacketPayload payload) {
      for (ServerPlayer player : PlayerLookup.tracking(level, pos)) {
         sendToPlayer(player, payload);
      }
   }
}

package buildcraft.fabric.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

public final class PacketDistributor {
   private PacketDistributor() {
   }

   public static void sendToServer(CustomPacketPayload payload) {
      ClientPlayNetworking.send(payload);
   }

   public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
      ServerPlayNetworking.send(player, payload);
   }

   public static void sendToPlayersInDimension(ServerLevel level, CustomPacketPayload payload) {
      for (ServerPlayer player : PlayerLookup.level(level)) {
         ServerPlayNetworking.send(player, payload);
      }
   }

   public static void sendToPlayersTrackingChunk(ServerLevel level, ChunkPos chunkPos, CustomPacketPayload payload) {
      BlockPos pos = chunkPos.getMiddleBlockPosition(level.getMinY());

      for (ServerPlayer player : PlayerLookup.tracking(level, pos)) {
         ServerPlayNetworking.send(player, payload);
      }
   }
}

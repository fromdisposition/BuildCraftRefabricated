package buildcraft.lib.misc;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

public class MessageUtil {
   public static void sendUpdateToTrackingPlayers(BlockEntity be) {
      if (be.getLevel() instanceof ServerLevel level) {
         Packet<?> packet = be.getUpdatePacket();
         if (packet == null) {
            return;
         }

         for (ServerPlayer player : PlayerLookup.tracking(level, be.getBlockPos())) {
            player.connection.send(packet);
         }
      }
   }

   public static void sendOverlayMessage(Player player, Component message) {
      player.sendOverlayMessage(message);
   }

   public static void sendSystemMessage(Player player, Component message) {
      player.sendSystemMessage(message);
   }
}

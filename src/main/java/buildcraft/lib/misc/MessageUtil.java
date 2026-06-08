/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

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

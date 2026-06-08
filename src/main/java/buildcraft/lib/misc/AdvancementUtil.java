/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import buildcraft.api.core.BCLog;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class AdvancementUtil {
   private static final Set<Identifier> UNKNOWN_ADVANCEMENTS = new HashSet<>();

   public static void unlockAdvancement(Player player, Identifier advancementName) {
      unlockAdvancement(player, advancementName, "code_trigger");
   }

   public static void unlockAdvancement(Player player, Identifier advancementName, String criterionName) {
      if (player instanceof ServerPlayer serverPlayer) {
         MinecraftServer server = player.level().getServer();
         if (server == null) {
            return;
         }

         ServerAdvancementManager advancementManager = server.getAdvancements();
         AdvancementHolder holder = advancementManager.get(advancementName);
         if (holder != null) {
            PlayerAdvancements tracker = serverPlayer.getAdvancements();
            tracker.award(holder, criterionName);
         } else if (UNKNOWN_ADVANCEMENTS.add(advancementName)) {
            BCLog.logger.warn("[lib.advancement] Attempted to trigger undefined advancement: " + advancementName);
         }
      }
   }

   public static boolean unlockAdvancement(UUID playerId, Level level, Identifier advancementName) {
      return unlockAdvancement(playerId, level, advancementName, "code_trigger");
   }

   public static boolean unlockAdvancement(UUID playerId, Level level, Identifier advancementName, String criterionName) {
      if (level.isClientSide()) {
         return false;
      } else {
         MinecraftServer server = level.getServer();
         if (server == null) {
            return false;
         } else {
            ServerPlayer player = server.getPlayerList().getPlayer(playerId);
            if (player != null) {
               unlockAdvancement(player, advancementName, criterionName);
               return true;
            } else {
               return false;
            }
         }
      }
   }
}

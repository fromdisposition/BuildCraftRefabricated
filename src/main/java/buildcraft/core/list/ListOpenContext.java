/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.list;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

public final class ListOpenContext {
   private static final Map<UUID, InteractionHand> NEXT_HAND = new ConcurrentHashMap<>();

   private ListOpenContext() {
   }

   public static void remember(Player player, InteractionHand hand) {
      NEXT_HAND.put(player.getUUID(), hand);
   }

   public static InteractionHand consume(Player player) {
      return NEXT_HAND.remove(player.getUUID());
   }
}

/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

// Routes machine-driven attacks through {@link AttackEntityCallback} so land-claim mods can authorize them like a
// player swing; a non-FAIL result with no listener objecting preserves vanilla behaviour.
public final class AttackEntityCompat {
   private AttackEntityCompat() {
   }

   public static boolean canAttack(Level level, Player player, Entity target) {
      InteractionResult result = AttackEntityCallback.EVENT.invoker().interact(player, level, InteractionHand.MAIN_HAND, target, null);
      return result != InteractionResult.FAIL;
   }
}

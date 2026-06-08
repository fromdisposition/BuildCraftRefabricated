/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import buildcraft.api.tools.IToolWrench;
import javax.annotation.Nullable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;

public class EntityUtil {
   @Nullable
   public static InteractionHand getWrenchHand(LivingEntity entity) {
      ItemStack main = entity.getItemInHand(InteractionHand.MAIN_HAND);
      if (!main.isEmpty() && main.getItem() instanceof IToolWrench) {
         return InteractionHand.MAIN_HAND;
      }

      ItemStack off = entity.getItemInHand(InteractionHand.OFF_HAND);
      return !off.isEmpty() && off.getItem() instanceof IToolWrench ? InteractionHand.OFF_HAND : null;
   }

   public static void activateWrench(Player player, HitResult trace) {
      ItemStack main = player.getItemInHand(InteractionHand.MAIN_HAND);
      if (!main.isEmpty() && main.getItem() instanceof IToolWrench wrench) {
         wrench.wrenchUsed(player, InteractionHand.MAIN_HAND, main, trace);
      } else {
         ItemStack off = player.getItemInHand(InteractionHand.OFF_HAND);
         if (!off.isEmpty() && off.getItem() instanceof IToolWrench wrench) {
            wrench.wrenchUsed(player, InteractionHand.OFF_HAND, off, trace);
         }
      }
   }
}

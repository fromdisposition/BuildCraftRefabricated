/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import org.jspecify.annotations.Nullable;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;

public class EntityUtil {
   /** The Fabric convention tag every wrench belongs to (ConventionalItemTags.WRENCH_TOOLS = c:tools/wrench).
    * BuildCraft's own wrench is in this tag too, so detection is uniform and fully native: any tagged wrench --
    * ours or another mod's -- counts, and no BuildCraft-specific interface is needed. */
   public static final TagKey<Item> WRENCHES = TagKey.create(Registries.ITEM, Identifier.parse("c:tools/wrench"));
   private static final Identifier WRENCH_ADVANCEMENT = Identifier.parse("buildcraftcore:wrenched");

   /** True if the stack is a wrench, decided purely by the native {@code c:tools/wrench} tag. */
   public static boolean isWrench(ItemStack stack) {
      return !stack.isEmpty() && stack.is(WRENCHES);
   }

   /** Common feedback for a successful wrench action: award the "wrenched" advancement and swing the arm. */
   public static void wrenchUsed(Player player, InteractionHand hand, ItemStack wrench, @Nullable HitResult trace) {
      AdvancementUtil.unlockAdvancement(player, WRENCH_ADVANCEMENT);
      player.swing(hand);
   }

   @Nullable
   public static InteractionHand getWrenchHand(LivingEntity entity) {
      if (isWrench(entity.getItemInHand(InteractionHand.MAIN_HAND))) {
         return InteractionHand.MAIN_HAND;
      }

      return isWrench(entity.getItemInHand(InteractionHand.OFF_HAND)) ? InteractionHand.OFF_HAND : null;
   }

   public static void activateWrench(Player player, HitResult trace) {
      ItemStack main = player.getItemInHand(InteractionHand.MAIN_HAND);
      if (isWrench(main)) {
         wrenchUsed(player, InteractionHand.MAIN_HAND, main, trace);
      } else {
         ItemStack off = player.getItemInHand(InteractionHand.OFF_HAND);
         if (isWrench(off)) {
            wrenchUsed(player, InteractionHand.OFF_HAND, off, trace);
         }
      }
   }
}

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
   /** The Fabric convention tag for wrenches (ConventionalItemTags.WRENCH_TOOLS = c:tools/wrench) -- the official,
    * forward-looking standard. BuildCraft's own wrench is in this tag. */
   public static final TagKey<Item> WRENCHES = TagKey.create(Registries.ITEM, Identifier.parse("c:tools/wrench"));
   /** The older de-facto wrench tag still used across the industrial ecosystem (Tech Reborn, Modern
    * Industrialization, Applied Energistics, ...). BuildCraft's wrench is added to this one too, and we accept it,
    * so wrenching is two-way with those mods -- there is no single universal wrench tag, so we honour both. */
   public static final TagKey<Item> WRENCHES_LEGACY = TagKey.create(Registries.ITEM, Identifier.parse("c:wrenches"));
   private static final Identifier WRENCH_ADVANCEMENT = Identifier.parse("buildcraftcore:wrenched");

   /** True if the stack is a wrench, by either convention tag ({@code c:tools/wrench} or the legacy {@code c:wrenches}). */
   public static boolean isWrench(ItemStack stack) {
      return !stack.isEmpty() && (stack.is(WRENCHES) || stack.is(WRENCHES_LEGACY));
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

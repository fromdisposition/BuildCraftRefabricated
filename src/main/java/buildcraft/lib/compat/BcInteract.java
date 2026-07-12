/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.compat;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
//? if < 1.21.10 {
/*import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
*///?}

/**
 * Bridges the block {@code useItemOn} return-type cliff. On 1.21.5+ it returns {@link InteractionResult};
 * on 1.21.1 it returns {@code ItemInteractionResult} (a separate enum, and 1.21.1's InteractionResult has
 * no TRY_WITH_EMPTY_HAND). BuildCraft blocks keep their logic in a neutral {@code bcUseItemOn(...)} that
 * returns InteractionResult, and a thin {@code useItemOn} wrapper does {@code return toItem(bcUseItemOn(...))}.
 * The wrapper's return type is switched to ItemInteractionResult on 1.21.1 by a stonecutter replace.
 */
public final class BcInteract {
   //? if >= 1.21.10 {
   /** "Item didn't act, defer to the empty-hand interaction." Real value on modern. */
   public static final InteractionResult TRY_WITH_EMPTY_HAND = InteractionResult.TRY_WITH_EMPTY_HAND;

   /** Identity on modern (useItemOn already returns InteractionResult). */
   public static InteractionResult toItem(InteractionResult result) {
      return result;
   }

   /** Identity on modern (Item.use already returns InteractionResult). */
   public static InteractionResult toUse(InteractionResult result, Player player, InteractionHand hand) {
      return result;
   }
   //?} else {
   /*// 1.21.1 InteractionResult has no TRY_WITH_EMPTY_HAND; PASS carries the same "defer to use()" meaning.
   public static final InteractionResult TRY_WITH_EMPTY_HAND = InteractionResult.PASS;

   public static ItemInteractionResult toItem(InteractionResult result) {
      return switch (result) {
         case SUCCESS -> ItemInteractionResult.SUCCESS;
         case CONSUME -> ItemInteractionResult.CONSUME;
         case CONSUME_PARTIAL -> ItemInteractionResult.CONSUME_PARTIAL;
         case FAIL -> ItemInteractionResult.FAIL;
         default -> ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
      };
   }

   // 1.21.1 Item.use returns InteractionResultHolder<ItemStack>; wrap the neutral result around the held stack.
   public static InteractionResultHolder<ItemStack> toUse(InteractionResult result, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      return switch (result) {
         case SUCCESS -> InteractionResultHolder.success(stack);
         case CONSUME -> InteractionResultHolder.consume(stack);
         case FAIL -> InteractionResultHolder.fail(stack);
         default -> InteractionResultHolder.pass(stack);
      };
   }
   *///?}

   private BcInteract() {
   }
}

/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.item;

import buildcraft.lib.fabric.BCLibClientBridge;
import buildcraft.lib.misc.AdvancementUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;

public class ItemGuide extends Item {
   private static final Identifier ADVANCEMENT = Identifier.parse("buildcraftcore:guide");
   private final String bookName;

   public ItemGuide(Properties properties, String bookName) {
      super(properties);
      this.bookName = bookName;
   }

   //? if >= 1.21.10 {
   public InteractionResult use(Level level, Player player, InteractionHand hand) {
      return this.bcUse(level, player, hand);
   }
   //?} else {
   /*public net.minecraft.world.InteractionResultHolder<net.minecraft.world.item.ItemStack> use(Level level, Player player, InteractionHand hand) {
      return buildcraft.lib.compat.BcInteract.toUse(this.bcUse(level, player, hand), player, hand);
   }
   *///?}

   private InteractionResult bcUse(Level level, Player player, InteractionHand hand) {
      if (level.isClientSide()) {
         BCLibClientBridge.openGuideScreen(this.bookName);
      } else {
         AdvancementUtil.unlockAdvancement(player, ADVANCEMENT);
      }

      return InteractionResult.SUCCESS;
   }
}

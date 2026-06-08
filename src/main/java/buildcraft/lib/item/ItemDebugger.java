/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.item;

import buildcraft.lib.debug.BCAdvDebugging;
import buildcraft.lib.debug.IAdvDebugTarget;
import buildcraft.lib.misc.MessageUtil;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;

public class ItemDebugger extends Item {
   public ItemDebugger(Properties properties) {
      super(properties);
   }

   public InteractionResult useOn(UseOnContext context) {
      if (context.getLevel().getBlockEntity(context.getClickedPos()) instanceof IAdvDebugTarget target) {
         if (context.getLevel().isClientSide()) {
            BCAdvDebugging.INSTANCE.setClientTarget(context.getClickedPos());
         } else if (context.getPlayer() != null) {
            MessageUtil.sendOverlayMessage(context.getPlayer(), target.getAdvDebugMessage());
         }

         return InteractionResult.SUCCESS;
      } else {
         return InteractionResult.PASS;
      }
   }

   public static boolean isShowDebugInfo(Player player) {
      return player.getAbilities().instabuild
         || player.getMainHandItem().getItem() instanceof ItemDebugger
         || player.getOffhandItem().getItem() instanceof ItemDebugger;
   }
}

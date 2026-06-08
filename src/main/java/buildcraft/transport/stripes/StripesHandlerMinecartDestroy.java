/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.stripes;

import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.IStripesHandlerBlock;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public enum StripesHandlerMinecartDestroy implements IStripesHandlerBlock {
   INSTANCE;

   @Override
   public boolean handle(Level world, BlockPos pos, Direction direction, Player player, IStripesActivator activator) {
      AABB box = new AABB(pos.relative(direction));
      List<AbstractMinecart> minecarts = world.getEntitiesOfClass(AbstractMinecart.class, box);
      if (minecarts.isEmpty()) {
         return false;
      }

      Collections.shuffle(minecarts);
      AbstractMinecart cart = minecarts.get(0);
      if (cart instanceof Container container) {
         for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack s = container.getItem(i);
            if (!s.isEmpty()) {
               container.setItem(i, ItemStack.EMPTY);
               if (container.getItem(i).isEmpty()) {
                  activator.sendItem(s, direction);
               }
            }
         }
      }

      ItemStack cartItem = cart.getPickResult();
      cart.discard();
      if (cartItem != null && !cartItem.isEmpty()) {
         activator.sendItem(cartItem, direction);
      }

      return true;
   }
}

/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.transport;

import buildcraft.api.core.EnumHandlerPriority;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface IStripesRegistry {
   default void addHandler(IStripesHandlerItem handler) {
      this.addHandler(handler, EnumHandlerPriority.NORMAL);
   }

   void addHandler(IStripesHandlerItem var1, EnumHandlerPriority var2);

   default void addHandler(IStripesHandlerBlock handler) {
      this.addHandler(handler, EnumHandlerPriority.NORMAL);
   }

   void addHandler(IStripesHandlerBlock var1, EnumHandlerPriority var2);

   boolean handleItem(Level var1, BlockPos var2, Direction var3, ItemStack var4, Player var5, IStripesActivator var6);

   boolean handleBlock(Level var1, BlockPos var2, Direction var3, Player var4, IStripesActivator var5);
}

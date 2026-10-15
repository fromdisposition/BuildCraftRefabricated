/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.transport.pipe;

import buildcraft.api.core.IStackFilter;
import buildcraft.api.transport.IInjectable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

public interface IFlowItems extends IInjectable {
   @Deprecated
   default int tryExtractItems(int count, Direction from, @Nullable DyeColor colour, IStackFilter filter) {
      return this.tryExtractItems(count, from, colour, filter, false);
   }

   int tryExtractItems(int var1, Direction var2, @Nullable DyeColor var3, IStackFilter var4, boolean var5);

   void insertItemsForce(@Nonnull ItemStack var1, Direction var2, @Nullable DyeColor var3, double var4);

   void sendPhantomItem(@Nonnull ItemStack var1, @Nullable Direction var2, @Nullable Direction var3, @Nullable DyeColor var4);
}

/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.core;

import javax.annotation.Nonnull;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

public interface IStackFilter {
   boolean matches(@Nonnull ItemStack var1);

   default IStackFilter and(IStackFilter filter) {
      IStackFilter before = this;
      return stack -> before.matches(stack) && filter.matches(stack);
   }

   default NonNullList<ItemStack> getExamples() {
      return NonNullList.withSize(0, ItemStack.EMPTY);
   }
}

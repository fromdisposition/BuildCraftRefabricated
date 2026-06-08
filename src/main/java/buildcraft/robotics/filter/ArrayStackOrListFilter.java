/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.filter;

import buildcraft.api.core.IStackFilter;
import buildcraft.lib.misc.StackUtil;
import javax.annotation.Nonnull;
import net.minecraft.world.item.ItemStack;

public class ArrayStackOrListFilter implements IStackFilter {
   protected final ItemStack[] stacks;

   public ArrayStackOrListFilter(ItemStack... stacks) {
      this.stacks = stacks;
   }

   @Override
   public boolean matches(@Nonnull ItemStack stack) {
      if (this.stacks.length == 0 || !this.hasFilter()) {
         return true;
      }

      for (ItemStack reference : this.stacks) {
         if (reference != null && !reference.isEmpty() && StackUtil.isMatchingItemOrList(reference, stack)) {
            return true;
         }
      }

      return false;
   }

   public boolean matches(IStackFilter other) {
      for (ItemStack reference : this.stacks) {
         if (reference != null && !reference.isEmpty() && other.matches(reference)) {
            return true;
         }
      }

      return false;
   }

   public boolean hasFilter() {
      for (ItemStack reference : this.stacks) {
         if (reference != null && !reference.isEmpty()) {
            return true;
         }
      }

      return false;
   }
}

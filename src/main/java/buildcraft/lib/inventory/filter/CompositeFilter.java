/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.inventory.filter;

import buildcraft.api.core.IStackFilter;
import javax.annotation.Nonnull;
import net.minecraft.world.item.ItemStack;

public class CompositeFilter implements IStackFilter {
   private final IStackFilter[] filters;

   public CompositeFilter(IStackFilter... iFilters) {
      this.filters = iFilters;
   }

   @Override
   public boolean matches(@Nonnull ItemStack stack) {
      for (IStackFilter f : this.filters) {
         if (f.matches(stack)) {
            return true;
         }
      }

      return false;
   }
}

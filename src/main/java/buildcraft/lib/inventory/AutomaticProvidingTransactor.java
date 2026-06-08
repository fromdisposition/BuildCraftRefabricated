/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.inventory;

import buildcraft.api.core.IStackFilter;
import buildcraft.api.inventory.IItemTransactor;
import buildcraft.lib.misc.StackUtil;
import javax.annotation.Nonnull;
import net.minecraft.world.item.ItemStack;

public enum AutomaticProvidingTransactor implements IItemTransactor.IItemExtractable {
   INSTANCE;

   @Nonnull
   @Override
   public ItemStack extract(IStackFilter filter, int min, int max, boolean simulate) {
      return StackUtil.EMPTY;
   }
}

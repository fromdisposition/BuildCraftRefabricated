/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.facades;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public interface IFacadeItem {
   @Nullable
   default FacadeType getFacadeType(@Nonnull ItemStack stack) {
      IFacade facade = this.getFacade(stack);
      return facade == null ? null : facade.getType();
   }

   @Nonnull
   ItemStack getFacadeForBlock(BlockState var1);

   ItemStack createFacadeStack(IFacade var1);

   @Nullable
   IFacade getFacade(@Nonnull ItemStack var1);
}

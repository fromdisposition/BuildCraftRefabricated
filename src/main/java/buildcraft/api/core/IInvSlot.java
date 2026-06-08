/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.core;

import net.minecraft.world.item.ItemStack;

public interface IInvSlot {
   int getIndex();

   boolean canPutStackInSlot(ItemStack var1);

   boolean canTakeStackFromSlot(ItemStack var1);

   boolean isItemValidForSlot(ItemStack var1);

   ItemStack decreaseStackInSlot(int var1);

   ItemStack getStackInSlot();

   void setStackInSlot(ItemStack var1);
}

/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.items;

import net.minecraft.world.item.ItemStack;

public interface IItemCustomPipeRender {
   float getPipeRenderScale(ItemStack var1);

   boolean renderItemInPipe(ItemStack var1, double var2, double var4, double var6);
}

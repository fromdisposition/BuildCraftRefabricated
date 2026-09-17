/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.transport;

import javax.annotation.Nonnull;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

public interface IInjectable {
   boolean canInjectItems(Direction var1);

   @Nonnull
   ItemStack injectItem(@Nonnull ItemStack var1, boolean var2, Direction var3, DyeColor var4, double var5);
}

/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.items;

import javax.annotation.Nonnull;
import net.minecraft.world.item.ItemStack;

public interface IList extends INamedItem {
   boolean matches(@Nonnull ItemStack var1, @Nonnull ItemStack var2);
}

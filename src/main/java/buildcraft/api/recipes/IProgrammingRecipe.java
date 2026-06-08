/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.recipes;

import java.util.List;
import net.minecraft.world.item.ItemStack;

public interface IProgrammingRecipe {
   String getId();

   List<ItemStack> getOptions(int width, int height);

   long getEnergyCostMj(ItemStack option);

   boolean canCraft(ItemStack input);

   ItemStack craft(ItemStack input, ItemStack option);
}

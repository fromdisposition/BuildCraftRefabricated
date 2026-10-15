/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts.recipe;

import buildcraft.lib.client.guide.parts.GuidePartFactory;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.world.item.ItemStack;

public interface IStackRecipes {
   List<GuidePartFactory> getUsages(@Nonnull ItemStack var1);

   List<GuidePartFactory> getRecipes(@Nonnull ItemStack var1);
}

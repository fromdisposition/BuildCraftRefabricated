/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.recipes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

public interface IIntegrationRecipeProvider {
   @Nullable
   IntegrationRecipe getRecipeFor(@Nonnull ItemStack var1, @Nonnull NonNullList<ItemStack> var2);

   IntegrationRecipe getRecipe(@Nonnull Object var1);
}

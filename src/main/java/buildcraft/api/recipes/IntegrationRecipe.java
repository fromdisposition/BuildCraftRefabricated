/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.recipes;

import com.google.common.collect.ImmutableList;
import javax.annotation.Nonnull;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

public abstract class IntegrationRecipe {
   public final Object name;

   public IntegrationRecipe(Object name) {
      this.name = name;
   }

   public abstract ItemStack getOutput(@Nonnull ItemStack var1, NonNullList<ItemStack> var2);

   public abstract ImmutableList<IngredientStack> getRequirements(@Nonnull ItemStack var1);

   public abstract long getRequiredMicroJoules(ItemStack var1);

   public abstract IngredientStack getCenterStack();

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         IntegrationRecipe that = (IntegrationRecipe)o;
         return this.name.equals(that.name);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return this.name.hashCode();
   }
}

/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.recipes;

import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public abstract class AssemblyRecipe implements Comparable<AssemblyRecipe> {
   private String registryName;

   public abstract Set<ItemStack> getOutputs(NonNullList<ItemStack> var1);

   public abstract Set<ItemStack> getOutputPreviews();

   public abstract Set<IngredientStack> getInputsFor(@Nonnull ItemStack var1);

   public abstract long getRequiredMicroJoulesFor(@Nonnull ItemStack var1);

   public final AssemblyRecipe setRegistryName(String name) {
      this.registryName = name;
      return this;
   }

   public final AssemblyRecipe setRegistryName(Object name) {
      if (name == null) {
         throw new IllegalArgumentException("Registry name cannot be null");
      } else if (name instanceof Identifier id) {
         return this.setRegistryName(id.toString());
      } else if (name instanceof String s) {
         return this.setRegistryName(s);
      } else {
         return this.setRegistryName(name.toString());
      }
   }

   public String getRegistryName() {
      return this.registryName;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AssemblyRecipe that) ? false : Objects.equals(this.registryName, that.registryName);
      }
   }

   @Override
   public int hashCode() {
      return this.registryName != null ? this.registryName.hashCode() : 0;
   }

   public int compareTo(AssemblyRecipe o) {
      return this.registryName != null && o.registryName != null ? this.registryName.toString().compareTo(o.registryName.toString()) : 0;
   }
}

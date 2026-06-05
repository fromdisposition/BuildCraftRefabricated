/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 */

package buildcraft.api.recipes;

import java.util.Set;

import javax.annotation.Nonnull;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

import buildcraft.api.recipes.IngredientStack;

public abstract class AssemblyRecipe implements Comparable<AssemblyRecipe> {
    private String registryName;

    public abstract Set<ItemStack> getOutputs(NonNullList<ItemStack> inputs);

    public abstract Set<ItemStack> getOutputPreviews();

    public abstract Set<IngredientStack> getInputsFor(@Nonnull ItemStack output);

    public abstract long getRequiredMicroJoulesFor(@Nonnull ItemStack output);

    public final AssemblyRecipe setRegistryName(String name) {
        this.registryName = name;
        return this;
    }

    public final AssemblyRecipe setRegistryName(Object name) {

        return this;
    }

    public String getRegistryName() {
        return registryName;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof AssemblyRecipe))
            return false;
        AssemblyRecipe that = (AssemblyRecipe) obj;
        return java.util.Objects.equals(registryName, that.registryName);
    }

    @Override
    public int hashCode() {
        return registryName != null ? registryName.hashCode() : 0;
    }

    @Override
    public int compareTo(AssemblyRecipe o) {
        if (registryName == null || o.registryName == null)
            return 0;
        return registryName.toString().compareTo(o.registryName.toString());
    }
}

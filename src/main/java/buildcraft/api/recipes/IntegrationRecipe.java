package buildcraft.api.recipes;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;

import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;

public abstract class IntegrationRecipe {
    public final Object name;

    public IntegrationRecipe(Object name) {
        this.name = name;
    }

    public abstract ItemStack getOutput(@Nonnull ItemStack target, NonNullList<ItemStack> toIntegrate);

    public abstract ImmutableList<IngredientStack> getRequirements(@Nonnull ItemStack output);

    public abstract long getRequiredMicroJoules(ItemStack output);

    public abstract IngredientStack getCenterStack();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        IntegrationRecipe that = (IntegrationRecipe) o;

        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}

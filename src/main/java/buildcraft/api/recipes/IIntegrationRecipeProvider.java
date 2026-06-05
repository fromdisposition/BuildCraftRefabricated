package buildcraft.api.recipes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;

public interface IIntegrationRecipeProvider {

    @Nullable
    IntegrationRecipe getRecipeFor(@Nonnull ItemStack target, @Nonnull NonNullList<ItemStack> toIntegrate);

    IntegrationRecipe getRecipe(@Nonnull Object name);
}

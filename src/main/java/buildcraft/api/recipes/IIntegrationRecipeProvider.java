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

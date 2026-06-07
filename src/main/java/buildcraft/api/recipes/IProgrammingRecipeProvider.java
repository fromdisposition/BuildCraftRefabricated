package buildcraft.api.recipes;

import javax.annotation.Nullable;
import net.minecraft.world.item.ItemStack;

public interface IProgrammingRecipeProvider {
   Iterable<IProgrammingRecipe> getRecipes();

   @Nullable
   IProgrammingRecipe getRecipe(String id);

   @Nullable
   IProgrammingRecipe getRecipeFor(ItemStack input);
}

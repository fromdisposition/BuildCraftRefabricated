package buildcraft.lib.recipe;

import buildcraft.api.recipes.IIntegrationRecipeRegistry;
import buildcraft.api.recipes.IntegrationRecipe;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

public class IntegrationRecipeRegistry implements IIntegrationRecipeRegistry {
   public static final IntegrationRecipeRegistry INSTANCE = new IntegrationRecipeRegistry();
   public final Map<Object, IntegrationRecipe> recipes = new HashMap<>();

   @Override
   public IntegrationRecipe getRecipeFor(@Nonnull ItemStack target, @Nonnull NonNullList<ItemStack> toIntegrate) {
      for (IntegrationRecipe recipe : this.recipes.values()) {
         if (!recipe.getOutput(target, toIntegrate).isEmpty()) {
            return recipe;
         }
      }

      return null;
   }

   @Override
   public void addRecipe(IntegrationRecipe recipe) {
      if (this.recipes.putIfAbsent(recipe.name, recipe) == null) {
         ;
      }
   }

   @Override
   public Iterable<IntegrationRecipe> getAllRecipes() {
      return this.recipes.values();
   }

   @Override
   public IntegrationRecipe getRecipe(@Nonnull Object name) {
      return this.recipes.get(name);
   }
}

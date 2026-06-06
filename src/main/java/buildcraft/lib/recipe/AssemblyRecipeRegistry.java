package buildcraft.lib.recipe;

import buildcraft.api.recipes.AssemblyRecipe;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

public class AssemblyRecipeRegistry {
   public static final Map<String, AssemblyRecipe> REGISTRY = new HashMap<>();

   public static void register(AssemblyRecipe recipe) {
      REGISTRY.put(recipe.getRegistryName(), recipe);
   }

   @Nonnull
   public static List<AssemblyRecipe> getRecipesFor(@Nonnull NonNullList<ItemStack> possibleIn) {
      List<AssemblyRecipe> all = new ArrayList<>();

      for (AssemblyRecipe ar : REGISTRY.values()) {
         if (!ar.getOutputs(possibleIn).isEmpty()) {
            all.add(ar);
         }
      }

      return all;
   }
}

package buildcraft.lib.recipe;

import buildcraft.lib.fabric.mixin.ShapelessRecipeAccessor;
import java.util.List;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;

public final class ShapelessRecipes {
   private ShapelessRecipes() {
   }

   public static List<Ingredient> ingredients(ShapelessRecipe recipe) {
      return ((ShapelessRecipeAccessor)recipe).buildcraft$getIngredients();
   }
}

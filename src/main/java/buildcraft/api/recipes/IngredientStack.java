package buildcraft.api.recipes;

import net.minecraft.world.item.crafting.Ingredient;

public final class IngredientStack {
   public final Ingredient ingredient;
   public final int count;

   public IngredientStack(Ingredient ingredient, int count) {
      this.ingredient = ingredient;
      this.count = count;
   }

   public IngredientStack(Ingredient ingredient) {
      this(ingredient, 1);
   }
}

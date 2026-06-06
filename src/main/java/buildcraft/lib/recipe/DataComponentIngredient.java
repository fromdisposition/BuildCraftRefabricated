package buildcraft.lib.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public final class DataComponentIngredient {
   private DataComponentIngredient() {
   }

   public static Ingredient of(ItemStack stack) {
      return Ingredient.of(stack.getItem());
   }
}

package buildcraft.lib.recipe;

import java.util.List;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;

import buildcraft.lib.fabric.mixin.ShapelessRecipeAccessor;

public final class ShapelessRecipes {
    private ShapelessRecipes() {}

    public static List<Ingredient> ingredients(ShapelessRecipe recipe) {
        return ((ShapelessRecipeAccessor) recipe).buildcraft$getIngredients();
    }
}

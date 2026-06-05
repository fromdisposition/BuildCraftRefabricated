package buildcraft.api.recipes;

import java.util.Iterator;

public interface IIntegrationRecipeRegistry extends IIntegrationRecipeProvider {
    void addRecipe(IntegrationRecipe recipe);

    Iterable<IntegrationRecipe> getAllRecipes();

}

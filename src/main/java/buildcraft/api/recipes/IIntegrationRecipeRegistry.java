package buildcraft.api.recipes;

public interface IIntegrationRecipeRegistry extends IIntegrationRecipeProvider {
   void addRecipe(IntegrationRecipe var1);

   Iterable<IntegrationRecipe> getAllRecipes();
}

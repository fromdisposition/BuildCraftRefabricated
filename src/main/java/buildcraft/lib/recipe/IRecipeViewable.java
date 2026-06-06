package buildcraft.lib.recipe;

public interface IRecipeViewable {
   ChangingItemStack[] getRecipeInputs();

   ChangingItemStack getRecipeOutputs();

   interface IRecipePowered extends IRecipeViewable {
      ChangingObject<Long> getMjCost();
   }

   interface IViewableGrid extends IRecipeViewable {
      int getRecipeWidth();

      int getRecipeHeight();
   }
}

package buildcraft.factory.integration.jei;

import buildcraft.api.recipes.IRefineryRecipeManager;
import mezz.jei.api.recipe.types.IRecipeType;

public final class DistillerRecipeTypes {
   public static final IRecipeType<IRefineryRecipeManager.IDistillationRecipe> DISTILLER = IRecipeType.create(
      "buildcraftfactory", "distiller", IRefineryRecipeManager.IDistillationRecipe.class
   );

   private DistillerRecipeTypes() {
   }
}

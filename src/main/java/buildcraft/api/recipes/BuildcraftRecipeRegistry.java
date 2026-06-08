package buildcraft.api.recipes;

import javax.annotation.Nullable;

/** Recipe registries populated by BuildCraft modules during startup. */
public final class BuildcraftRecipeRegistry {
   @Nullable
   public static IIntegrationRecipeRegistry integrationRecipes;
   @Nullable
   public static IRefineryRecipeManager refineryRecipes;
   @Nullable
   public static IProgrammingRecipeRegistry programmingTable;

   private BuildcraftRecipeRegistry() {
   }
}

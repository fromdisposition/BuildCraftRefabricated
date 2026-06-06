package buildcraft.silicon.integration.jei;

import mezz.jei.api.recipe.types.IRecipeType;

public final class AssemblyRecipeJeiTypes {
   public static final IRecipeType<AssemblyRecipeJei> ASSEMBLY = IRecipeType.create("buildcraftsilicon", "assembly_table", AssemblyRecipeJei.class);

   private AssemblyRecipeJeiTypes() {
   }
}

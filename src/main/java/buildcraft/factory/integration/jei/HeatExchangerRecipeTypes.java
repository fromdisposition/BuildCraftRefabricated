package buildcraft.factory.integration.jei;

import mezz.jei.api.recipe.types.IRecipeType;

public final class HeatExchangerRecipeTypes {
   public static final IRecipeType<HeatExchangerRecipePair> PAIR = IRecipeType.create("buildcraftfactory", "heat_exchanger", HeatExchangerRecipePair.class);

   private HeatExchangerRecipeTypes() {
   }
}

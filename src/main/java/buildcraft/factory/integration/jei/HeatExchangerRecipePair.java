package buildcraft.factory.integration.jei;

import buildcraft.api.recipes.IRefineryRecipeManager;

public record HeatExchangerRecipePair(IRefineryRecipeManager.IHeatableRecipe heatable, IRefineryRecipeManager.ICoolableRecipe coolable) {
}

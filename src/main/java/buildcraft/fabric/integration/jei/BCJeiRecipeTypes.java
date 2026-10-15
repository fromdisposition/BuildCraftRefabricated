/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.fabric.integration.jei;

import buildcraft.api.recipes.IRefineryRecipeManager;
import buildcraft.energy.integration.jei.CombustionCoolantJei;
import buildcraft.energy.integration.jei.StirlingFuelJei;
import buildcraft.energy.recipe.CombustionFuelRecipe;
import buildcraft.factory.recipe.HeatExchangerRecipePair;
import buildcraft.silicon.recipe.AssemblyRecipeView;
import buildcraft.silicon.recipe.IntegrationRecipeView;
import buildcraft.silicon.recipe.ProgrammingRecipeView;

public final class BCJeiRecipeTypes {
   public static final mezz.jei.api.recipe.types.IRecipeType<AssemblyRecipeView> ASSEMBLY = mezz.jei.api.recipe.types.IRecipeType.create("buildcraftsilicon", "assembly_table", AssemblyRecipeView.class);
   public static final mezz.jei.api.recipe.types.IRecipeType<IntegrationRecipeView> INTEGRATION = mezz.jei.api.recipe.types.IRecipeType.create("buildcraftsilicon", "integration_table", IntegrationRecipeView.class);
   public static final mezz.jei.api.recipe.types.IRecipeType<ProgrammingRecipeView> PROGRAMMING = mezz.jei.api.recipe.types.IRecipeType.create("buildcraftsilicon", "programming_table", ProgrammingRecipeView.class);
   public static final mezz.jei.api.recipe.types.IRecipeType<CombustionFuelRecipe> COMBUSTION_FUEL = mezz.jei.api.recipe.types.IRecipeType.create("buildcraftenergy", "combustion_engine_fuel", CombustionFuelRecipe.class);
   public static final mezz.jei.api.recipe.types.IRecipeType<CombustionCoolantJei> COMBUSTION_COOLANT = mezz.jei.api.recipe.types.IRecipeType.create(
      "buildcraftenergy", "combustion_engine_coolant", CombustionCoolantJei.class
   );
   public static final mezz.jei.api.recipe.types.IRecipeType<StirlingFuelJei> STIRLING_FUEL = mezz.jei.api.recipe.types.IRecipeType.create("buildcraftenergy", "stirling_engine_fuel", StirlingFuelJei.class);
   public static final mezz.jei.api.recipe.types.IRecipeType<IRefineryRecipeManager.IDistillationRecipe> DISTILLER = mezz.jei.api.recipe.types.IRecipeType.create(
      "buildcraftfactory", "distiller", IRefineryRecipeManager.IDistillationRecipe.class
   );
   public static final mezz.jei.api.recipe.types.IRecipeType<HeatExchangerRecipePair> HEAT_EXCHANGER = mezz.jei.api.recipe.types.IRecipeType.create(
      "buildcraftfactory", "heat_exchanger", HeatExchangerRecipePair.class
   );

   private BCJeiRecipeTypes() {
   }
}

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
import buildcraft.factory.integration.jei.HeatExchangerRecipePair;
import buildcraft.silicon.integration.jei.AssemblyRecipeJei;
import buildcraft.silicon.integration.jei.IntegrationRecipeJei;
import buildcraft.silicon.integration.jei.ProgrammingRecipeJei;

public final class BCJeiRecipeTypes {
   public static final mezz.jei.api.recipe.types.IRecipeType<AssemblyRecipeJei> ASSEMBLY = mezz.jei.api.recipe.types.IRecipeType.create("buildcraftsilicon", "assembly_table", AssemblyRecipeJei.class);
   public static final mezz.jei.api.recipe.types.IRecipeType<IntegrationRecipeJei> INTEGRATION = mezz.jei.api.recipe.types.IRecipeType.create("buildcraftsilicon", "integration_table", IntegrationRecipeJei.class);
   public static final mezz.jei.api.recipe.types.IRecipeType<ProgrammingRecipeJei> PROGRAMMING = mezz.jei.api.recipe.types.IRecipeType.create("buildcraftsilicon", "programming_table", ProgrammingRecipeJei.class);
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

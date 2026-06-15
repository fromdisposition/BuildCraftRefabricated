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
import mezz.jei.api.recipe.types.IRecipeType;

public final class BCJeiRecipeTypes {
   public static final IRecipeType<AssemblyRecipeJei> ASSEMBLY = IRecipeType.create("buildcraftsilicon", "assembly_table", AssemblyRecipeJei.class);
   public static final IRecipeType<IntegrationRecipeJei> INTEGRATION = IRecipeType.create("buildcraftsilicon", "integration_table", IntegrationRecipeJei.class);
   public static final IRecipeType<ProgrammingRecipeJei> PROGRAMMING = IRecipeType.create("buildcraftsilicon", "programming_table", ProgrammingRecipeJei.class);
   public static final IRecipeType<CombustionFuelRecipe> COMBUSTION_FUEL = IRecipeType.create("buildcraftenergy", "combustion_engine_fuel", CombustionFuelRecipe.class);
   public static final IRecipeType<CombustionCoolantJei> COMBUSTION_COOLANT = IRecipeType.create(
      "buildcraftenergy", "combustion_engine_coolant", CombustionCoolantJei.class
   );
   public static final IRecipeType<StirlingFuelJei> STIRLING_FUEL = IRecipeType.create("buildcraftenergy", "stirling_engine_fuel", StirlingFuelJei.class);
   public static final IRecipeType<IRefineryRecipeManager.IDistillationRecipe> DISTILLER = IRecipeType.create(
      "buildcraftfactory", "distiller", IRefineryRecipeManager.IDistillationRecipe.class
   );
   public static final IRecipeType<HeatExchangerRecipePair> HEAT_EXCHANGER = IRecipeType.create(
      "buildcraftfactory", "heat_exchanger", HeatExchangerRecipePair.class
   );

   private BCJeiRecipeTypes() {
   }
}

/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib;

import buildcraft.api.fuels.BuildcraftFuelRegistry;
import buildcraft.api.recipes.BuildcraftRecipeRegistry;
import buildcraft.lib.fabric.BCLibFakePlayerProvider;
import buildcraft.lib.fluid.CoolantRegistry;
import buildcraft.lib.fluid.FuelRegistry;
import buildcraft.lib.fluids.CauldronFluidContent;
import buildcraft.lib.recipe.IntegrationRecipeRegistry;
import buildcraft.lib.recipe.ProgrammingRecipeRegistry;
import buildcraft.lib.recipe.RefineryRecipeRegistry;
import buildcraft.lib.registry.MigrationRegistry;

public final class BCLib {
   public static final String MODID = "buildcraftlib";
   public static final boolean DEV = Boolean.getBoolean("buildcraft.dev");

   private BCLib() {
   }

   public static void init() {
      initApiRegistries();
      BCLibItems.register();
      CauldronFluidContent.init();
      BCLibFakePlayerProvider.register();
      MigrationRegistry.init();
   }

   private static void initApiRegistries() {
      if (BuildcraftFuelRegistry.fuel == null) {
         BuildcraftFuelRegistry.fuel = FuelRegistry.INSTANCE;
      }

      if (BuildcraftFuelRegistry.coolant == null) {
         BuildcraftFuelRegistry.coolant = CoolantRegistry.INSTANCE;
      }

      if (BuildcraftRecipeRegistry.refineryRecipes == null) {
         BuildcraftRecipeRegistry.refineryRecipes = RefineryRecipeRegistry.INSTANCE;
      }

      if (BuildcraftRecipeRegistry.integrationRecipes == null) {
         BuildcraftRecipeRegistry.integrationRecipes = IntegrationRecipeRegistry.INSTANCE;
      }

      if (BuildcraftRecipeRegistry.programmingTable == null) {
         BuildcraftRecipeRegistry.programmingTable = ProgrammingRecipeRegistry.INSTANCE;
      }
   }
}

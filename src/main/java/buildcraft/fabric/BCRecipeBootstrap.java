/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.fabric;

import buildcraft.api.facades.FacadeAPI;
import buildcraft.energy.BCEnergyRecipes;
import buildcraft.silicon.BCSiliconIntegrationRecipes;
import buildcraft.silicon.BCSiliconItems;
import buildcraft.silicon.BCSiliconPlugs;
import buildcraft.silicon.BCSiliconRecipes;
import buildcraft.silicon.plug.FacadeStateManager;

/** Called after registries are ready, also from the JEI plugin; must not reference JEI types since JEI is optional. */
public final class BCRecipeBootstrap {
   private BCRecipeBootstrap() {
   }

   public static void initSiliconRecipes() {
      BCSiliconPlugs.registerAll();
      FacadeAPI.facadeItem = BCSiliconItems.PLUG_FACADE;
      FacadeAPI.registry = FacadeStateManager.INSTANCE;
      FacadeStateManager.ensureInitialized();
      BCSiliconRecipes.init();
      BCSiliconIntegrationRecipes.init();
   }

   public static void initEnergyRecipes() {
      BCEnergyRecipes.init();
   }
}

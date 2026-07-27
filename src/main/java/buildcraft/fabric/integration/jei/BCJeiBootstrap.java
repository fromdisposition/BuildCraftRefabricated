/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.fabric.integration.jei;

import buildcraft.api.facades.FacadeAPI;
import buildcraft.energy.BCEnergyRecipes;
import buildcraft.silicon.BCSiliconIntegrationRecipes;
import buildcraft.silicon.BCSiliconItems;
import buildcraft.silicon.BCSiliconPlugs;
import buildcraft.silicon.BCSiliconRecipes;
import buildcraft.silicon.plug.FacadeStateManager;

/**
 * Registers in-memory BC recipe tables once registries and holder components are ready.
 * Called on {@code SERVER_STARTING} for gameplay and from JEI {@code registerRecipes} on the client.
 * JEI is optional ({@code compileOnly}); this class has no JEI API dependency.
 */
public final class BCJeiBootstrap {
   private BCJeiBootstrap() {
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

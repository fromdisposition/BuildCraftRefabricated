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
 * Registers in-memory BC recipe tables once prerequisites are ready.
 * Silicon tables are item-only and init during mod registration (original BC FML init timing).
 * Energy/refinery tables use {@link buildcraft.lib.fluids.FluidStack} and init on {@code SERVER_STARTING}
 * and lazily from JEI {@code registerRecipes} on the client.
 * JEI is optional ({@code compileOnly}); this class has no JEI API dependency.
 */
public final class BCJeiBootstrap {
   private BCJeiBootstrap() {
   }

   /** Item-based assembly/integration recipes — safe after {@code BCSiliconFabric.register()}. */
   public static void initSiliconRecipes() {
      BCSiliconPlugs.registerAll();
      FacadeAPI.facadeItem = BCSiliconItems.PLUG_FACADE;
      FacadeAPI.registry = FacadeStateManager.INSTANCE;
      FacadeStateManager.ensureInitialized();
      BCSiliconRecipes.init();
      BCSiliconIntegrationRecipes.init();
   }

   /** Fluid/fuel/refinery recipes — requires bound fluid holder components. */
   public static void initEnergyRecipes() {
      BCEnergyRecipes.init();
   }
}

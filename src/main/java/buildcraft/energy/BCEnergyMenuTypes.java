/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy;

import buildcraft.energy.container.ContainerDynamoMJ;
import buildcraft.energy.container.ContainerEngineIron_BC8;
import buildcraft.energy.container.ContainerEngineRF;
import buildcraft.energy.container.ContainerEngineStone_BC8;
import buildcraft.fabric.BCRegistries;
import buildcraft.lib.fabric.menu.ExtendedMenuTypes;
import net.minecraft.world.inventory.MenuType;

public final class BCEnergyMenuTypes {
   public static MenuType<ContainerEngineStone_BC8> ENGINE_STONE;
   public static MenuType<ContainerEngineIron_BC8> ENGINE_IRON;
   public static MenuType<ContainerEngineRF> ENGINE_FE;
   public static MenuType<ContainerDynamoMJ> DYNAMO_MJ;

   private BCEnergyMenuTypes() {
   }

   public static void register() {
      ENGINE_STONE = BCRegistries.registerMenuType("buildcraftenergy", "engine_stone", ExtendedMenuTypes.create(ContainerEngineStone_BC8::new));
      ENGINE_IRON = BCRegistries.registerMenuType("buildcraftenergy", "engine_iron", ExtendedMenuTypes.create(ContainerEngineIron_BC8::new));
      if (BCEnergyBlocks.ENGINE_FE != null) {
         ENGINE_FE = BCRegistries.registerMenuType("buildcraftenergy", "engine_rf", ExtendedMenuTypes.create(ContainerEngineRF::new));
      }

      if (BCEnergyBlocks.DYNAMO_MJ != null) {
         DYNAMO_MJ = BCRegistries.registerMenuType("buildcraftenergy", "mj_dynamo", ExtendedMenuTypes.create(ContainerDynamoMJ::new));
      }
   }
}

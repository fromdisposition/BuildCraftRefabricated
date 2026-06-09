/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory;

import buildcraft.fabric.BCRegistries;
import buildcraft.lib.BCLib;
import buildcraft.factory.container.ContainerAutoCraftFluids;
import buildcraft.factory.container.ContainerAutoCraftItems;
import buildcraft.factory.container.ContainerChute;
import buildcraft.factory.container.ContainerDistiller;
import buildcraft.factory.container.ContainerHeatExchange;
import buildcraft.factory.container.ContainerTank;
import buildcraft.lib.fabric.menu.ExtendedMenuTypes;
import net.minecraft.world.inventory.MenuType;

public final class BCFactoryMenuTypes {
   public static MenuType<ContainerAutoCraftItems> AUTO_WORKBENCH_ITEMS;
   public static MenuType<ContainerAutoCraftFluids> AUTO_WORKBENCH_FLUIDS;
   public static MenuType<ContainerTank> TANK;
   public static MenuType<ContainerChute> CHUTE;
   public static MenuType<ContainerDistiller> DISTILLER;
   public static MenuType<ContainerHeatExchange> HEAT_EXCHANGE;

   private BCFactoryMenuTypes() {
   }

   public static void register() {
      AUTO_WORKBENCH_ITEMS = BCRegistries.registerMenuType("buildcraftfactory", "auto_workbench_items", ExtendedMenuTypes.create(ContainerAutoCraftItems::new));
      if (BCLib.DEV) {
         AUTO_WORKBENCH_FLUIDS = BCRegistries.registerMenuType("buildcraftfactory", "auto_workbench_fluids", ExtendedMenuTypes.create(ContainerAutoCraftFluids::new));
      }

      TANK = BCRegistries.registerMenuType("buildcraftfactory", "tank", ExtendedMenuTypes.create(ContainerTank::new));
      CHUTE = BCRegistries.registerMenuType("buildcraftfactory", "chute", ExtendedMenuTypes.create(ContainerChute::new));
      DISTILLER = BCRegistries.registerMenuType("buildcraftfactory", "distiller", ExtendedMenuTypes.create(ContainerDistiller::new));
      HEAT_EXCHANGE = BCRegistries.registerMenuType("buildcraftfactory", "heat_exchange", ExtendedMenuTypes.create(ContainerHeatExchange::new));
   }
}

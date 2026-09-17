/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics;

import buildcraft.fabric.BCRegistries;
import buildcraft.lib.fabric.menu.ExtendedMenuTypes;
import buildcraft.robotics.container.ContainerRequester;
import buildcraft.robotics.container.ContainerZonePlanner;
import net.minecraft.world.inventory.MenuType;

public final class BCRoboticsMenuTypes {
   public static MenuType<ContainerZonePlanner> ZONE_PLANNER;
   public static MenuType<ContainerRequester> REQUESTER;

   private BCRoboticsMenuTypes() {
   }

   public static void register() {
      ZONE_PLANNER = BCRegistries.registerMenuType("buildcraftrobotics", "zone_planner", ExtendedMenuTypes.create(ContainerZonePlanner::new));
      REQUESTER = BCRegistries.registerMenuType("buildcraftrobotics", "requester", ExtendedMenuTypes.create(ContainerRequester::new));
   }
}

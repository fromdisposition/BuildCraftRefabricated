package buildcraft.robotics;

import buildcraft.fabric.BCRegistries;
import buildcraft.lib.BCLib;
import buildcraft.lib.fabric.menu.ExtendedMenuTypes;
import buildcraft.robotics.container.ContainerZonePlanner;
import net.minecraft.world.inventory.MenuType;

public final class BCRoboticsMenuTypes {
   public static MenuType<ContainerZonePlanner> ZONE_PLANNER;

   private BCRoboticsMenuTypes() {
   }

   public static void register() {
      if (BCLib.DEV) {
         ZONE_PLANNER = BCRegistries.registerMenuType("buildcraftrobotics", "zone_planner", ExtendedMenuTypes.create(ContainerZonePlanner::new));
      }
   }
}

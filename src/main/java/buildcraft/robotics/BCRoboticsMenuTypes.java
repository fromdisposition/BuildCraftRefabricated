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

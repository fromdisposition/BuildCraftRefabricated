package buildcraft.fabric;

import buildcraft.lib.BCLib;
import buildcraft.lib.client.BCTooltips;
import buildcraft.robotics.BCRoboticsItems;
import buildcraft.robotics.BCRoboticsMenuTypes;
import buildcraft.robotics.gui.GuiZonePlanner;
import net.minecraft.client.gui.screens.MenuScreens;

public final class BCRoboticsFabricClient {
   private BCRoboticsFabricClient() {
   }

   public static void init() {
      if (BCLib.DEV && BCRoboticsMenuTypes.ZONE_PLANNER != null) {
         MenuScreens.register(BCRoboticsMenuTypes.ZONE_PLANNER, GuiZonePlanner::new);
         BCTooltips.markDevOnly(BCRoboticsItems.ZONE_PLANNER);
      }
   }
}

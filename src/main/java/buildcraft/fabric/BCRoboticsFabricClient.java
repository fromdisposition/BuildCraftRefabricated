package buildcraft.fabric;

import net.minecraft.client.gui.screens.MenuScreens;

import buildcraft.lib.BCLib;
import buildcraft.robotics.BCRoboticsItems;
import buildcraft.robotics.BCRoboticsMenuTypes;
import buildcraft.robotics.gui.GuiZonePlanner;

public final class BCRoboticsFabricClient {
    private BCRoboticsFabricClient() {}

    public static void init() {
        if (BCLib.DEV && BCRoboticsMenuTypes.ZONE_PLANNER != null) {
            MenuScreens.register(BCRoboticsMenuTypes.ZONE_PLANNER, GuiZonePlanner::new);
            buildcraft.lib.client.BCTooltips.markDevOnly(BCRoboticsItems.ZONE_PLANNER.get());
        }
    }
}

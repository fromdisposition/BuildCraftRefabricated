package buildcraft.fabric;

import buildcraft.robotics.BCRoboticsBlockEntities;
import buildcraft.robotics.BCRoboticsBlocks;
import buildcraft.robotics.BCRoboticsItems;
import buildcraft.robotics.BCRoboticsMenuTypes;

public final class BCRoboticsFabric {
    private BCRoboticsFabric() {}

    public static void register() {
        BCRoboticsBlocks.register();
        BCRoboticsItems.register();
        BCRoboticsBlockEntities.register();
        BCRoboticsMenuTypes.register();
    }
}


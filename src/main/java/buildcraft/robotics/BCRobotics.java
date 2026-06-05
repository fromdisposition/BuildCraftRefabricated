package buildcraft.robotics;

import buildcraft.core.BCCore;
import buildcraft.fabric.BCRoboticsFabric;

public final class BCRobotics {
    public static final String MODID = "buildcraftrobotics";

    private BCRobotics() {}

    public static void init() {
        BCRoboticsFabric.register();
    }
}

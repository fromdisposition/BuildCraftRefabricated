package buildcraft.robotics;

import buildcraft.fabric.registry.DeferredRegister;
import buildcraft.fabric.registry.DeferredItem;

import buildcraft.lib.BCLib;

public class BCRoboticsItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(BCRobotics.MODID);

    public static final DeferredItem<?> ZONE_PLANNER;

    static {
        ZONE_PLANNER = (BCLib.DEV && BCRoboticsBlocks.ZONE_PLANNER != null)
                ? ITEMS.registerSimpleBlockItem(BCRoboticsBlocks.ZONE_PLANNER)
                : null;
    }

    public static void register() {
        ITEMS.register();
    }
}


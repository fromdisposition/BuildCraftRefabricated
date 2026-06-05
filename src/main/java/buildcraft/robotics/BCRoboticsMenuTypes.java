package buildcraft.robotics;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;

import buildcraft.fabric.BCRegistries;
import buildcraft.lib.BCLib;
import buildcraft.lib.fabric.menu.ExtendedMenuTypes;
import buildcraft.robotics.container.ContainerZonePlanner;

public final class BCRoboticsMenuTypes {
    public static MenuType<ContainerZonePlanner> ZONE_PLANNER;

    private BCRoboticsMenuTypes() {}

    public static void register() {
        if (BCLib.DEV) {
            ZONE_PLANNER = Registry.register(
                    BuiltInRegistries.MENU,
                    BCRegistries.id(BCRobotics.MODID, "zone_planner"),
                    ExtendedMenuTypes.create(ContainerZonePlanner::new));
        }
    }
}

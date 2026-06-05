package buildcraft.robotics;

import net.minecraft.world.level.block.entity.BlockEntityType;

import buildcraft.fabric.BCRegistries;
import buildcraft.lib.BCLib;
import buildcraft.robotics.tile.TileZonePlanner;

public final class BCRoboticsBlockEntities {
    public static BlockEntityType<TileZonePlanner> ZONE_PLANNER;

    private BCRoboticsBlockEntities() {}

    public static void register() {
        if (BCLib.DEV && BCRoboticsBlocks.ZONE_PLANNER != null) {
            ZONE_PLANNER = BCRegistries.registerBlockEntity(BCRobotics.MODID, 
                    "zone_planner", TileZonePlanner::new, BCRoboticsBlocks.ZONE_PLANNER.get());
        }
    }
}

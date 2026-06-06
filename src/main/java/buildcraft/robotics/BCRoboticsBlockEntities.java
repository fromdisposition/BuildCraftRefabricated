package buildcraft.robotics;

import buildcraft.fabric.BCRegistries;
import buildcraft.lib.BCLib;
import buildcraft.robotics.tile.TileZonePlanner;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class BCRoboticsBlockEntities {
   public static BlockEntityType<TileZonePlanner> ZONE_PLANNER;

   private BCRoboticsBlockEntities() {
   }

   public static void register() {
      if (BCLib.DEV && BCRoboticsBlocks.ZONE_PLANNER != null) {
         ZONE_PLANNER = BCRegistries.registerBlockEntity("buildcraftrobotics", "zone_planner", TileZonePlanner::new, BCRoboticsBlocks.ZONE_PLANNER);
      }
   }
}

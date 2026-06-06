package buildcraft.robotics;

import buildcraft.fabric.BCRegistries;
import buildcraft.lib.BCLib;
import net.minecraft.world.item.BlockItem;

public final class BCRoboticsItems {
   public static BlockItem ZONE_PLANNER;

   private BCRoboticsItems() {
   }

   public static void register() {
      if (BCLib.DEV && BCRoboticsBlocks.ZONE_PLANNER != null) {
         ZONE_PLANNER = BCRegistries.registerBlockItem("buildcraftrobotics", "zone_planner", BCRoboticsBlocks.ZONE_PLANNER);
      }
   }
}

package buildcraft.robotics;

import buildcraft.fabric.BCRegistries;
import buildcraft.lib.BCLib;
import buildcraft.robotics.block.BlockZonePlanner;
import net.minecraft.world.level.block.SoundType;

public final class BCRoboticsBlocks {
   public static BlockZonePlanner ZONE_PLANNER;

   private BCRoboticsBlocks() {
   }

   public static void register() {
      if (BCLib.DEV) {
         ZONE_PLANNER = BCRegistries.registerBlock(
            "buildcraftrobotics", "zone_planner", BlockZonePlanner::new, p -> p.strength(5.0F, 10.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()
         );
      }
   }
}

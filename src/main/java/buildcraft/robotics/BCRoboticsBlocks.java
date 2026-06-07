package buildcraft.robotics;

import buildcraft.fabric.BCRegistries;
import buildcraft.robotics.block.BlockRequester;
import buildcraft.robotics.block.BlockZonePlanner;
import net.minecraft.world.level.block.SoundType;

public final class BCRoboticsBlocks {
   public static BlockZonePlanner ZONE_PLANNER;
   public static BlockRequester REQUESTER;

   private BCRoboticsBlocks() {
   }

   public static void register() {
      ZONE_PLANNER = BCRegistries.registerBlock(
         "buildcraftrobotics", "zone_planner", BlockZonePlanner::new, p -> p.strength(5.0F, 10.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()
      );
      REQUESTER = BCRegistries.registerBlock(
         "buildcraftrobotics", "requester", BlockRequester::new, p -> p.strength(5.0F, 10.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()
      );
   }
}

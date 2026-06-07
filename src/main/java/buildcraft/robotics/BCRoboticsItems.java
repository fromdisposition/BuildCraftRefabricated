package buildcraft.robotics;

import buildcraft.fabric.BCRegistries;
import buildcraft.robotics.item.ItemPluggableRobotStation;
import buildcraft.robotics.item.ItemRobot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

public final class BCRoboticsItems {
   public static BlockItem ZONE_PLANNER;
   public static BlockItem REQUESTER;
   public static Item REDSTONE_BOARD;
   public static Item ROBOT_STATION;
   public static Item ROBOT;

   private BCRoboticsItems() {
   }

   public static void register() {
      REDSTONE_BOARD = BCRegistries.registerItem("buildcraftrobotics", "redstone_board", props -> new ItemRedstoneBoard(props.stacksTo(16)));
      ROBOT_STATION = BCRegistries.registerItem("buildcraftrobotics", "robot_station", ItemPluggableRobotStation::new);
      ROBOT = BCRegistries.registerItem("buildcraftrobotics", "robot", ItemRobot::new);
      if (BCRoboticsBlocks.ZONE_PLANNER != null) {
         ZONE_PLANNER = BCRegistries.registerBlockItem("buildcraftrobotics", "zone_planner", BCRoboticsBlocks.ZONE_PLANNER);
      }

      if (BCRoboticsBlocks.REQUESTER != null) {
         REQUESTER = BCRegistries.registerBlockItem("buildcraftrobotics", "requester", BCRoboticsBlocks.REQUESTER);
      }
   }
}

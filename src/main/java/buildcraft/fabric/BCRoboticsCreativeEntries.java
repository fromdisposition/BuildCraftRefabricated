package buildcraft.fabric;

import buildcraft.api.boards.RedstoneBoardNBT;
import buildcraft.api.boards.RedstoneBoardRegistry;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.ItemRedstoneBoard;
import buildcraft.robotics.item.ItemRobot;
import buildcraft.robotics.BCRoboticsItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab.Output;

public final class BCRoboticsCreativeEntries {
   private BCRoboticsCreativeEntries() {
   }

   public static void addMainTabItems(Output output) {
      accept(output, BCRoboticsItems.REDSTONE_BOARD);
      accept(output, BCRoboticsItems.ZONE_PLANNER);
      accept(output, BCRoboticsItems.REQUESTER);
      accept(output, BCRoboticsItems.ROBOT_STATION);
      accept(output, BCRoboticsItems.ROBOT);
      addProgrammed(output);
   }

   // A blank robot cannot be deployed, so the plain item alone gives creative no way to place one.
   private static void addProgrammed(Output output) {
      if (BCRoboticsItems.REDSTONE_BOARD == null || BCRoboticsItems.ROBOT == null
         || RedstoneBoardRegistry.instance == null) {
         return;
      }

      for (RedstoneBoardNBT<?> board : RedstoneBoardRegistry.instance.getAllBoardNBTs()) {
         if (board instanceof RedstoneBoardRobotNBT robotBoard) {
            output.accept(ItemRedstoneBoard.createStack(robotBoard));
            output.accept(ItemRobot.createRobotStack(robotBoard, EntityRobotBase.MAX_POWER));
         }
      }
   }

   private static void accept(Output output, Item item) {
      if (item != null) {
         output.accept(item);
      }
   }
}

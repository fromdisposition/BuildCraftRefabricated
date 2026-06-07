package buildcraft.robotics.boards;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.ai.AIRobotFetchItem;
import buildcraft.robotics.ai.AIRobotGotoSleep;
import buildcraft.robotics.ai.AIRobotGotoStationAndUnload;
import buildcraft.robotics.statement.StationActions;

public class BoardRobotPicker extends RedstoneBoardRobot {
   public BoardRobotPicker(EntityRobotBase robot) {
      super(robot);
   }

   private void fetchNewItem() {
      this.startDelegateAI(new AIRobotFetchItem(this.robot, 250.0F,
         StationActions.getGateFilter(this.robot.getLinkedStation()), this.robot.getZoneToWork()));
   }

   @Override
   public void update() {
      this.fetchNewItem();
   }

   @Override
   public void delegateAIEnded(AIRobot ai) {
      if (ai instanceof AIRobotFetchItem) {
         if (ai.success()) {
            this.fetchNewItem();
         } else if (this.robot.containsItems()) {
            this.startDelegateAI(new AIRobotGotoStationAndUnload(this.robot));
         } else {
            this.startDelegateAI(new AIRobotGotoSleep(this.robot));
         }
      } else if (ai instanceof AIRobotGotoStationAndUnload && !ai.success()) {
         this.startDelegateAI(new AIRobotGotoSleep(this.robot));
      }
   }

   @Override
   public RedstoneBoardRobotNBT getNBTHandler() {
      return BCBoardNBT.REGISTRY.get("picker");
   }
}

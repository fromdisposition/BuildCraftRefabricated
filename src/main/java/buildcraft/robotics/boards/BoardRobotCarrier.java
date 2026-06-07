package buildcraft.robotics.boards;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.core.IStackFilter;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.ai.AIRobotGotoSleep;
import buildcraft.robotics.ai.AIRobotGotoStationAndLoad;
import buildcraft.robotics.ai.AIRobotGotoStationAndUnload;
import buildcraft.robotics.ai.AIRobotLoad;
import buildcraft.robotics.statement.StationActions;

public class BoardRobotCarrier extends RedstoneBoardRobot {
   public BoardRobotCarrier(EntityRobotBase robot) {
      super(robot);
   }

   @Override
   public RedstoneBoardRobotNBT getNBTHandler() {
      return BCBoardNBT.REGISTRY.get("carrier");
   }

   @Override
   public void update() {
      if (!this.robot.containsItems()) {
         IStackFilter filter = StationActions.getGateFilter(this.robot.getLinkedStation());
         this.startDelegateAI(new AIRobotGotoStationAndLoad(this.robot, filter, AIRobotLoad.ANY_QUANTITY));
      } else {
         this.startDelegateAI(new AIRobotGotoStationAndUnload(this.robot));
      }
   }

   @Override
   public void delegateAIEnded(AIRobot ai) {
      if ((ai instanceof AIRobotGotoStationAndLoad || ai instanceof AIRobotGotoStationAndUnload) && !ai.success()) {
         this.startDelegateAI(new AIRobotGotoSleep(this.robot));
      }
   }
}

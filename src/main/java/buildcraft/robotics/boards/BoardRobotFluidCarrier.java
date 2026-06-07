package buildcraft.robotics.boards;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.ai.AIRobotGotoSleep;
import buildcraft.robotics.ai.AIRobotGotoStationAndLoadFluids;
import buildcraft.robotics.ai.AIRobotGotoStationAndUnloadFluids;
import buildcraft.robotics.path.IFluidFilter;
import buildcraft.robotics.statement.StationActions;

public class BoardRobotFluidCarrier extends RedstoneBoardRobot {
   public BoardRobotFluidCarrier(EntityRobotBase robot) {
      super(robot);
   }

   @Override
   public RedstoneBoardRobotNBT getNBTHandler() {
      return BCBoardNBT.REGISTRY.get("fluidCarrier");
   }

   @Override
   public void update() {
      if (!this.robot.hasFluid()) {
         IFluidFilter filter = StationActions.getGateFluidFilter(this.robot.getLinkedStation());
         this.startDelegateAI(new AIRobotGotoStationAndLoadFluids(this.robot, filter));
      } else {
         this.startDelegateAI(new AIRobotGotoStationAndUnloadFluids(this.robot));
      }
   }

   @Override
   public void delegateAIEnded(AIRobot ai) {
      if ((ai instanceof AIRobotGotoStationAndLoadFluids || ai instanceof AIRobotGotoStationAndUnloadFluids) && !ai.success()) {
         this.startDelegateAI(new AIRobotGotoSleep(this.robot));
      }
   }
}

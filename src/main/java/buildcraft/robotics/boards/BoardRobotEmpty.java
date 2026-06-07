package buildcraft.robotics.boards;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.ai.AIRobotSleep;

public class BoardRobotEmpty extends RedstoneBoardRobot {
   public BoardRobotEmpty(EntityRobotBase robot) {
      super(robot);
   }

   @Override
   public RedstoneBoardRobotNBT getNBTHandler() {
      return RedstoneBoardRobotEmptyNBT.INSTANCE;
   }

   @Override
   public void update() {
      this.startDelegateAI(new AIRobotSleep(this.robot));
   }
}

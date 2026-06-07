package buildcraft.robotics.boards;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.ai.AIRobotSleep;

/**
 * Common base for the concrete {@link RedstoneBoardRobot} board behaviours. Each subclass identifies itself by the
 * registry name used in {@link BCBoardNBT#REGISTRY}. The default {@link #update()} keeps the robot idle; concrete
 * board behaviour is layered on top by overriding it.
 */
public abstract class BoardRobotBC extends RedstoneBoardRobot {
   public BoardRobotBC(EntityRobotBase robot) {
      super(robot);
   }

   protected abstract String boardName();

   @Override
   public RedstoneBoardRobotNBT getNBTHandler() {
      return BCBoardNBT.REGISTRY.get(this.boardName());
   }

   @Override
   public void update() {
      this.startDelegateAI(new AIRobotSleep(this.robot));
   }
}

package buildcraft.api.boards;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;

public abstract class RedstoneBoardRobot extends AIRobot implements IRedstoneBoard<EntityRobotBase> {
   public RedstoneBoardRobot(EntityRobotBase iRobot) {
      super(iRobot);
   }

   public abstract RedstoneBoardRobotNBT getNBTHandler();

   public final void updateBoard(EntityRobotBase container) {
   }

   @Override
   public boolean canLoadFromNBT() {
      return true;
   }
}

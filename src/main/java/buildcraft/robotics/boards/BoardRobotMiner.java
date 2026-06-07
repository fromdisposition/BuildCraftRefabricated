package buildcraft.robotics.boards;

import buildcraft.api.robots.EntityRobotBase;

public class BoardRobotMiner extends BoardRobotBC {
   public BoardRobotMiner(EntityRobotBase robot) {
      super(robot);
   }

   @Override
   protected String boardName() {
      return "miner";
   }
}

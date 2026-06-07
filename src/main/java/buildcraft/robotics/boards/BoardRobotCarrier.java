package buildcraft.robotics.boards;

import buildcraft.api.robots.EntityRobotBase;

public class BoardRobotCarrier extends BoardRobotBC {
   public BoardRobotCarrier(EntityRobotBase robot) {
      super(robot);
   }

   @Override
   protected String boardName() {
      return "carrier";
   }
}

package buildcraft.robotics.boards;

import buildcraft.api.robots.EntityRobotBase;

public class BoardRobotStripes extends BoardRobotBC {
   public BoardRobotStripes(EntityRobotBase robot) {
      super(robot);
   }

   @Override
   protected String boardName() {
      return "stripes";
   }
}

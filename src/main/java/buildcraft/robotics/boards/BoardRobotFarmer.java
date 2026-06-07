package buildcraft.robotics.boards;

import buildcraft.api.robots.EntityRobotBase;

public class BoardRobotFarmer extends BoardRobotBC {
   public BoardRobotFarmer(EntityRobotBase robot) {
      super(robot);
   }

   @Override
   protected String boardName() {
      return "farmer";
   }
}

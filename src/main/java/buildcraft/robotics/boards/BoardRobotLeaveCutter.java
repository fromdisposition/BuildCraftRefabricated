package buildcraft.robotics.boards;

import buildcraft.api.robots.EntityRobotBase;

public class BoardRobotLeaveCutter extends BoardRobotBC {
   public BoardRobotLeaveCutter(EntityRobotBase robot) {
      super(robot);
   }

   @Override
   protected String boardName() {
      return "leaveCutter";
   }
}

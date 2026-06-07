package buildcraft.robotics.boards;

import buildcraft.api.robots.EntityRobotBase;

public class BoardRobotPump extends BoardRobotBC {
   public BoardRobotPump(EntityRobotBase robot) {
      super(robot);
   }

   @Override
   protected String boardName() {
      return "pump";
   }
}

package buildcraft.robotics.boards;

import buildcraft.api.robots.EntityRobotBase;

public class BoardRobotBomber extends BoardRobotBC {
   public BoardRobotBomber(EntityRobotBase robot) {
      super(robot);
   }

   @Override
   protected String boardName() {
      return "bomber";
   }
}

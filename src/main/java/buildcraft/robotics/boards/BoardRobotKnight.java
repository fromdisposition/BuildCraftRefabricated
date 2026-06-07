package buildcraft.robotics.boards;

import buildcraft.api.robots.EntityRobotBase;

public class BoardRobotKnight extends BoardRobotBC {
   public BoardRobotKnight(EntityRobotBase robot) {
      super(robot);
   }

   @Override
   protected String boardName() {
      return "knight";
   }
}

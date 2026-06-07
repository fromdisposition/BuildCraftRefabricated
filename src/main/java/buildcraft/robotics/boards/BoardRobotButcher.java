package buildcraft.robotics.boards;

import buildcraft.api.robots.EntityRobotBase;

public class BoardRobotButcher extends BoardRobotBC {
   public BoardRobotButcher(EntityRobotBase robot) {
      super(robot);
   }

   @Override
   protected String boardName() {
      return "butcher";
   }
}

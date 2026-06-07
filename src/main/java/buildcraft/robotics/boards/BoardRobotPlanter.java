package buildcraft.robotics.boards;

import buildcraft.api.robots.EntityRobotBase;

public class BoardRobotPlanter extends BoardRobotBC {
   public BoardRobotPlanter(EntityRobotBase robot) {
      super(robot);
   }

   @Override
   protected String boardName() {
      return "planter";
   }
}

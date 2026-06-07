package buildcraft.robotics.boards;

import buildcraft.api.robots.EntityRobotBase;

public class BoardRobotPicker extends BoardRobotBC {
   public BoardRobotPicker(EntityRobotBase robot) {
      super(robot);
   }

   @Override
   protected String boardName() {
      return "picker";
   }
}

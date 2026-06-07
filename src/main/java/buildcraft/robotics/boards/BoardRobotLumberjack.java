package buildcraft.robotics.boards;

import buildcraft.api.robots.EntityRobotBase;

public class BoardRobotLumberjack extends BoardRobotBC {
   public BoardRobotLumberjack(EntityRobotBase robot) {
      super(robot);
   }

   @Override
   protected String boardName() {
      return "lumberjack";
   }
}

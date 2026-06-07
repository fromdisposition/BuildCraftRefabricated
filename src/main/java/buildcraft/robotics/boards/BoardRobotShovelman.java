package buildcraft.robotics.boards;

import buildcraft.api.robots.EntityRobotBase;

public class BoardRobotShovelman extends BoardRobotBC {
   public BoardRobotShovelman(EntityRobotBase robot) {
      super(robot);
   }

   @Override
   protected String boardName() {
      return "shovelman";
   }
}

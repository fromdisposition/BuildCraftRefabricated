package buildcraft.robotics.boards;

import buildcraft.api.robots.EntityRobotBase;

public class BoardRobotDelivery extends BoardRobotBC {
   public BoardRobotDelivery(EntityRobotBase robot) {
      super(robot);
   }

   @Override
   protected String boardName() {
      return "delivery";
   }
}

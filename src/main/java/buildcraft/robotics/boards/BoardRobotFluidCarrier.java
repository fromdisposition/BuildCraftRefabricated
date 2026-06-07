package buildcraft.robotics.boards;

import buildcraft.api.robots.EntityRobotBase;

public class BoardRobotFluidCarrier extends BoardRobotBC {
   public BoardRobotFluidCarrier(EntityRobotBase robot) {
      super(robot);
   }

   @Override
   protected String boardName() {
      return "fluidCarrier";
   }
}

package buildcraft.robotics.boards;

import buildcraft.api.robots.EntityRobotBase;

public class BoardRobotHarvester extends BoardRobotBC {
   public BoardRobotHarvester(EntityRobotBase robot) {
      super(robot);
   }

   @Override
   protected String boardName() {
      return "harvester";
   }
}

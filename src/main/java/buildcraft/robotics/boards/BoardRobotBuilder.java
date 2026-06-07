package buildcraft.robotics.boards;

import buildcraft.api.robots.EntityRobotBase;

/**
 * Builder board. Full integration with the builders module (construction markers, blueprint requirements) is deferred;
 * for now the board idles like the other not-yet-specialised boards.
 */
public class BoardRobotBuilder extends BoardRobotBC {
   public BoardRobotBuilder(EntityRobotBase robot) {
      super(robot);
   }

   @Override
   protected String boardName() {
      return "builder";
   }
}

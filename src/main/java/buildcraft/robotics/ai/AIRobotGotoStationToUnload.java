package buildcraft.robotics.ai;

import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.IStationFilter;

/** Navigates to a station able to accept the robot's items (without unloading them yet). */
public class AIRobotGotoStationToUnload extends AIRobotGotoStationForInventory {
   public AIRobotGotoStationToUnload(EntityRobotBase robot) {
      super(robot, false);
   }

   @Override
   protected IStationFilter createStationFilter() {
      return station -> AIRobotUnload.unload(this.robot, station, false);
   }
}

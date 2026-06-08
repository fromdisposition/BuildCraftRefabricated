package buildcraft.robotics.ai;

import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.IStationFilter;

/** Navigates to a station that can accept the robot's stored fluid (without unloading it yet). */
public class AIRobotGotoStationToUnloadFluids extends AIRobotGotoStationForInventory {
   public AIRobotGotoStationToUnloadFluids(EntityRobotBase robot) {
      super(robot, true);
   }

   @Override
   protected IStationFilter createStationFilter() {
      return station -> AIRobotUnloadFluids.unload(this.robot, station, false) > 0;
   }
}

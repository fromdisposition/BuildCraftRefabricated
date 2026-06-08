package buildcraft.robotics.ai;

import buildcraft.api.core.IStackFilter;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.IStationFilter;

/** Navigates to a station that can provide items matching the filter (without loading them yet). */
public class AIRobotGotoStationToLoad extends AIRobotGotoStationForInventory {
   private IStackFilter filter;
   private int quantity;

   public AIRobotGotoStationToLoad(EntityRobotBase robot) {
      super(robot, true);
   }

   public AIRobotGotoStationToLoad(EntityRobotBase robot, IStackFilter filter, int quantity) {
      super(robot, true);
      this.filter = filter;
      this.quantity = quantity;
   }

   @Override
   protected IStationFilter createStationFilter() {
      return station -> AIRobotLoad.load(this.robot, station, this.filter, this.quantity, false);
   }
}

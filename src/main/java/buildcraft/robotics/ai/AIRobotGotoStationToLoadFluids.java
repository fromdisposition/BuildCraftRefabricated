package buildcraft.robotics.ai;

import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.IStationFilter;
import buildcraft.robotics.path.IFluidFilter;

/** Navigates to a station that can provide a fluid matching the filter (without loading it yet). */
public class AIRobotGotoStationToLoadFluids extends AIRobotGotoStationForInventory {
   private IFluidFilter filter;

   public AIRobotGotoStationToLoadFluids(EntityRobotBase robot) {
      super(robot, true);
   }

   public AIRobotGotoStationToLoadFluids(EntityRobotBase robot, IFluidFilter filter) {
      super(robot, true);
      this.filter = filter;
   }

   @Override
   protected IStationFilter createStationFilter() {
      return station -> AIRobotLoadFluids.load(this.robot, station, this.filter, false) > 0;
   }
}

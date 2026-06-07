package buildcraft.robotics.ai;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.IStationFilter;
import buildcraft.robotics.path.IFluidFilter;

/** Navigates to a station that can provide a fluid matching the filter (without loading it yet). */
public class AIRobotGotoStationToLoadFluids extends AIRobot {
   private IFluidFilter filter;

   public AIRobotGotoStationToLoadFluids(EntityRobotBase robot) {
      super(robot);
   }

   public AIRobotGotoStationToLoadFluids(EntityRobotBase robot, IFluidFilter filter) {
      this(robot);
      this.filter = filter;
   }

   @Override
   public void update() {
      this.startDelegateAI(new AIRobotSearchAndGotoStation(this.robot, new StationFilter(), this.robot.getZoneToLoadUnload()));
   }

   @Override
   public void delegateAIEnded(AIRobot ai) {
      if (ai instanceof AIRobotSearchAndGotoStation) {
         this.setSuccess(ai.success());
         this.terminate();
      }
   }

   private class StationFilter implements IStationFilter {
      @Override
      public boolean matches(DockingStation station) {
         return AIRobotLoadFluids.load(AIRobotGotoStationToLoadFluids.this.robot, station, AIRobotGotoStationToLoadFluids.this.filter, false) > 0;
      }
   }
}

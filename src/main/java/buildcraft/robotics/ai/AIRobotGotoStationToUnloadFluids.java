package buildcraft.robotics.ai;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.IStationFilter;

/** Navigates to a station that can accept the robot's stored fluid (without unloading it yet). */
public class AIRobotGotoStationToUnloadFluids extends AIRobot {
   public AIRobotGotoStationToUnloadFluids(EntityRobotBase robot) {
      super(robot);
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
         return AIRobotUnloadFluids.unload(AIRobotGotoStationToUnloadFluids.this.robot, station, false) > 0;
      }
   }
}

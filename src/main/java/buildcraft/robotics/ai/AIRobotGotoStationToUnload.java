package buildcraft.robotics.ai;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.IStationFilter;

/** Navigates to a station able to accept the robot's items (without unloading them yet). */
public class AIRobotGotoStationToUnload extends AIRobot {
   public AIRobotGotoStationToUnload(EntityRobotBase robot) {
      super(robot);
   }

   @Override
   public void start() {
      this.startDelegateAI(new AIRobotSearchAndGotoStation(this.robot, new StationInventory(), this.robot.getZoneToLoadUnload()));
   }

   @Override
   public void delegateAIEnded(AIRobot ai) {
      if (ai instanceof AIRobotSearchAndGotoStation) {
         this.setSuccess(ai.success());
         this.terminate();
      }
   }

   private class StationInventory implements IStationFilter {
      @Override
      public boolean matches(DockingStation station) {
         return AIRobotUnload.unload(AIRobotGotoStationToUnload.this.robot, station, false);
      }
   }
}

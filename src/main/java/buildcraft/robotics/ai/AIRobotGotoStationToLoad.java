package buildcraft.robotics.ai;

import buildcraft.api.core.IStackFilter;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.IStationFilter;

/** Navigates to a station that can provide items matching the filter (without loading them yet). */
public class AIRobotGotoStationToLoad extends AIRobot {
   private IStackFilter filter;
   private int quantity;

   public AIRobotGotoStationToLoad(EntityRobotBase robot) {
      super(robot);
   }

   public AIRobotGotoStationToLoad(EntityRobotBase robot, IStackFilter filter, int quantity) {
      this(robot);
      this.filter = filter;
      this.quantity = quantity;
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
         return AIRobotLoad.load(AIRobotGotoStationToLoad.this.robot, station, AIRobotGotoStationToLoad.this.filter, AIRobotGotoStationToLoad.this.quantity, false);
      }
   }
}

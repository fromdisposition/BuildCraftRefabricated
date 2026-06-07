package buildcraft.robotics.ai;

import buildcraft.api.core.IZone;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.IStationFilter;

/** Searches for a station matching a filter and then navigates to it. */
public class AIRobotSearchAndGotoStation extends AIRobot {
   private IStationFilter filter;
   private IZone zone;

   public AIRobotSearchAndGotoStation(EntityRobotBase robot) {
      super(robot);
   }

   public AIRobotSearchAndGotoStation(EntityRobotBase robot, IStationFilter filter, IZone zone) {
      this(robot);
      this.filter = filter;
      this.zone = zone;
   }

   @Override
   public void start() {
      this.startDelegateAI(new AIRobotSearchStation(this.robot, this.filter, this.zone));
   }

   @Override
   public void delegateAIEnded(AIRobot ai) {
      if (ai instanceof AIRobotSearchStation search) {
         if (ai.success()) {
            this.startDelegateAI(new AIRobotGotoStation(this.robot, search.targetStation));
         } else {
            this.setSuccess(false);
            this.terminate();
         }
      } else if (ai instanceof AIRobotGotoStation) {
         this.setSuccess(ai.success());
         this.terminate();
      }
   }
}

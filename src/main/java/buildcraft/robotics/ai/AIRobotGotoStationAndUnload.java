package buildcraft.robotics.ai;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.EntityRobotBase;

/** Navigates to an accepting station (or a given one) and then unloads the robot's items into it. */
public class AIRobotGotoStationAndUnload extends AIRobot {
   private DockingStation station;

   public AIRobotGotoStationAndUnload(EntityRobotBase robot) {
      super(robot);
   }

   public AIRobotGotoStationAndUnload(EntityRobotBase robot, DockingStation station) {
      super(robot);
      this.station = station;
   }

   @Override
   public void start() {
      if (this.station == null) {
         this.startDelegateAI(new AIRobotGotoStationToUnload(this.robot));
      } else {
         this.startDelegateAI(new AIRobotGotoStation(this.robot, this.station));
      }
   }

   @Override
   public void delegateAIEnded(AIRobot ai) {
      if (ai instanceof AIRobotGotoStationToUnload || ai instanceof AIRobotGotoStation) {
         if (ai.success()) {
            this.startDelegateAI(new AIRobotUnload(this.robot));
         } else {
            this.setSuccess(false);
            this.terminate();
         }
      } else if (ai instanceof AIRobotUnload) {
         this.setSuccess(ai.success());
         this.terminate();
      }
   }
}

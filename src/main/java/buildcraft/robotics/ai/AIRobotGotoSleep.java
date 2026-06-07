package buildcraft.robotics.ai;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;

/** Releases the robot's reserved resources, returns it to its linked station, and puts it to sleep. */
public class AIRobotGotoSleep extends AIRobot {
   public AIRobotGotoSleep(EntityRobotBase robot) {
      super(robot);
   }

   @Override
   public void start() {
      this.robot.releaseResources();
      if (this.robot.getLinkedStation() != null) {
         this.startDelegateAI(new AIRobotGotoStation(this.robot, this.robot.getLinkedStation()));
      } else {
         this.startDelegateAI(new AIRobotSleep(this.robot));
      }
   }

   @Override
   public void delegateAIEnded(AIRobot ai) {
      if (ai instanceof AIRobotGotoStation) {
         this.startDelegateAI(new AIRobotSleep(this.robot));
      } else if (ai instanceof AIRobotSleep) {
         this.terminate();
      }
   }
}

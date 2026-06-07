package buildcraft.robotics.ai;

import buildcraft.api.core.IStackFilter;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;

/** Navigates to a providing station and then loads the matching items onto the robot. */
public class AIRobotGotoStationAndLoad extends AIRobot {
   private IStackFilter filter;
   private int quantity;

   public AIRobotGotoStationAndLoad(EntityRobotBase robot) {
      super(robot);
   }

   public AIRobotGotoStationAndLoad(EntityRobotBase robot, IStackFilter filter, int quantity) {
      this(robot);
      this.filter = filter;
      this.quantity = quantity;
   }

   @Override
   public void start() {
      this.startDelegateAI(new AIRobotGotoStationToLoad(this.robot, this.filter, this.quantity));
   }

   @Override
   public void delegateAIEnded(AIRobot ai) {
      if (ai instanceof AIRobotGotoStationToLoad) {
         if (this.filter != null && ai.success()) {
            this.startDelegateAI(new AIRobotLoad(this.robot, this.filter, this.quantity));
         } else {
            this.setSuccess(false);
            this.terminate();
         }
      } else if (ai instanceof AIRobotLoad) {
         this.setSuccess(ai.success());
         this.terminate();
      }
   }
}

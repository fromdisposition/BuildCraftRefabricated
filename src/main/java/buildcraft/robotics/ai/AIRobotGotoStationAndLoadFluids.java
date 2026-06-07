package buildcraft.robotics.ai;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.path.IFluidFilter;

/** Navigates to a providing station and then loads matching fluid into the robot's tank. */
public class AIRobotGotoStationAndLoadFluids extends AIRobot {
   private IFluidFilter filter;

   public AIRobotGotoStationAndLoadFluids(EntityRobotBase robot) {
      super(robot);
   }

   public AIRobotGotoStationAndLoadFluids(EntityRobotBase robot, IFluidFilter filter) {
      this(robot);
      this.filter = filter;
   }

   @Override
   public void start() {
      this.startDelegateAI(new AIRobotGotoStationToLoadFluids(this.robot, this.filter));
   }

   @Override
   public void delegateAIEnded(AIRobot ai) {
      if (ai instanceof AIRobotGotoStationToLoadFluids) {
         if (this.filter != null && ai.success()) {
            this.startDelegateAI(new AIRobotLoadFluids(this.robot, this.filter));
         } else {
            this.setSuccess(false);
            this.terminate();
         }
      } else if (ai instanceof AIRobotLoadFluids) {
         this.setSuccess(ai.success());
         this.terminate();
      }
   }
}

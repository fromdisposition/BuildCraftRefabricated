package buildcraft.robotics.ai;

import buildcraft.api.core.BCLog;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;

/**
 * Root AI scheduling the robot's board. It pre-empts the board with overriding AIs (e.g. shutdown when out of power,
 * recharge when low, or one-off externally injected behaviours).
 */
public class AIRobotMain extends AIRobot {
   private AIRobot overridingAI;
   private int rechargeCooldown;

   public AIRobotMain(EntityRobotBase robot) {
      super(robot);
   }

   @Override
   public long getPowerCost() {
      return 0L;
   }

   @Override
   public void preempt(AIRobot ai) {
      long power = this.robot.getBattery().getStored();
      boolean dockProvidesPower = this.robot.getDockingStation() != null && this.robot.getDockingStation().providesPower();

      if (power <= EntityRobotBase.SHUTDOWN_POWER && !dockProvidesPower) {
         if (!(ai instanceof AIRobotShutdown)) {
            BCLog.logger.info("[robots] Shutting down robot " + this.robot + " - no power");
            this.startDelegateAI(new AIRobotShutdown(this.robot));
         }
      } else if (power < EntityRobotBase.SAFETY_POWER) {
         if (!(ai instanceof AIRobotRecharge) && !(ai instanceof AIRobotShutdown) && this.rechargeCooldown-- <= 0) {
            this.startDelegateAI(new AIRobotRecharge(this.robot));
         }
      } else if (!(ai instanceof AIRobotRecharge) && this.overridingAI != null && ai != this.overridingAI) {
         this.startDelegateAI(this.overridingAI);
      }
   }

   @Override
   public void update() {
      AIRobot board = this.robot.getBoard();
      if (board != null) {
         this.startDelegateAI(board);
      }
   }

   @Override
   public void delegateAIEnded(AIRobot ai) {
      if (ai instanceof AIRobotRecharge && !ai.success()) {
         this.rechargeCooldown = 120;
      }

      if (ai == this.overridingAI) {
         this.overridingAI = null;
      }
   }

   public void setOverridingAI(AIRobot ai) {
      if (this.overridingAI == null) {
         this.overridingAI = ai;
      }
   }

   public AIRobot getOverridingAI() {
      return this.overridingAI;
   }

   @Override
   public boolean canLoadFromNBT() {
      return true;
   }
}

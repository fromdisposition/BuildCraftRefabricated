package buildcraft.robotics.ai;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.EntityRobotBase;

/**
 * Sends a low-power robot to a station that can recharge it and waits there until topped up. Power-providing stations
 * are wired in a later phase; until then this simply fails fast (and the robot keeps working until it shuts down).
 */
public class AIRobotRecharge extends AIRobot {
   public AIRobotRecharge(EntityRobotBase robot) {
      super(robot);
      this.setSuccess(false);
   }

   @Override
   public void start() {
      DockingStation station = this.findChargingStation();
      if (station == null) {
         this.terminate();
      } else {
         this.startDelegateAI(new AIRobotGotoStation(this.robot, station));
      }
   }

   @Override
   public void delegateAIEnded(AIRobot ai) {
      if (ai instanceof AIRobotGotoStation && !ai.success()) {
         this.terminate();
      }
   }

   @Override
   public void update() {
      if (this.robot.getBattery().getStored() >= EntityRobotBase.MAX_POWER) {
         this.setSuccess(true);
         this.terminate();
      }
   }

   private DockingStation findChargingStation() {
      DockingStation linked = this.robot.getLinkedStation();
      if (linked != null && linked.providesPower()) {
         return linked;
      }

      DockingStation current = this.robot.getDockingStation();
      return current != null && current.providesPower() ? current : null;
   }

   @Override
   public long getPowerCost() {
      return 0L;
   }
}

/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.ai;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.robots.RobotIdleReason;

/** Go to a station that can serve an errand and carry it out there. Succeeds only if something actually moved. */
public class AIRobotRunErrand extends AIRobot {
   private StationErrand errand;

   public AIRobotRunErrand(EntityRobotBase robot) {
      super(robot);
   }

   public AIRobotRunErrand(EntityRobotBase robot, StationErrand errand) {
      this(robot);
      this.errand = errand;
   }

   /** Which errand this is, for boards that treat loading and unloading differently. */
   public StationErrand errand() {
      return this.errand;
   }

   @Override
   public void start() {
      if (this.errand == null || !this.errand.isValid()) {
         this.setSuccess(false);
         this.terminate();
         return;
      }

      this.startDelegateAI(new AIRobotGotoStationFor(this.robot, this.errand));
   }

   @Override
   public void delegateAIEnded(AIRobot ai) {
      if (ai instanceof AIRobotGotoStationFor) {
         if (ai.success()) {
            this.startDelegateAI(this.errand.work(this.robot));
         } else {
            this.setSuccess(false);
            this.terminate();
         }

         return;
      }

      this.setSuccess(ai.success());
      this.terminate();
   }

   @Override
   public void end() {
      RobotIdleReason reason = this.errand == null ? RobotIdleReason.NO_WORK : this.errand.idleReason();
      this.robot.reportProgress(this.success(), reason);
   }
}

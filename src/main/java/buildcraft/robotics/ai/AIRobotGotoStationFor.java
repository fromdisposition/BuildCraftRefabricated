/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.ai;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;

/** Find the nearest station that can serve an errand, inside the load/unload zone, and fly to it. */
public class AIRobotGotoStationFor extends AIRobot {
   private StationErrand errand;

   public AIRobotGotoStationFor(EntityRobotBase robot) {
      super(robot);
   }

   public AIRobotGotoStationFor(EntityRobotBase robot, StationErrand errand) {
      this(robot);
      this.errand = errand;
   }

   @Override
   public void start() {
      if (this.errand == null || !this.errand.isValid()) {
         this.setSuccess(false);
         this.terminate();
         return;
      }

      this.startDelegateAI(new AIRobotSearchAndGotoStation(
         this.robot,
         station -> this.errand.possibleAt(this.robot, station),
         this.robot.getZoneToLoadUnload()
      ));
   }

   @Override
   public void delegateAIEnded(AIRobot ai) {
      this.setSuccess(ai.success());
      this.terminate();
   }
}

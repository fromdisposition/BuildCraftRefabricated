/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.ai;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.EntityRobotBase;

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

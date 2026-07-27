/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.ai;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;

public class AIRobotGotoStationAndUnloadFluids extends AIRobot {
   public AIRobotGotoStationAndUnloadFluids(EntityRobotBase robot) {
      super(robot);
   }

   @Override
   public void start() {
      this.startDelegateAI(new AIRobotGotoStationToUnloadFluids(this.robot));
   }

   @Override
   public void delegateAIEnded(AIRobot ai) {
      if (ai instanceof AIRobotGotoStationToUnloadFluids) {
         if (ai.success()) {
            this.startDelegateAI(new AIRobotUnloadFluids(this.robot));
         } else {
            this.setSuccess(false);
            this.terminate();
         }
      } else if (ai instanceof AIRobotUnloadFluids) {
         this.setSuccess(ai.success());
         this.terminate();
      }
   }
}

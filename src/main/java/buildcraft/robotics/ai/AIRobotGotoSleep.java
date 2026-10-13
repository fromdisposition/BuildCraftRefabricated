/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.ai;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;

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

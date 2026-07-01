/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.ai;

import buildcraft.api.mj.MjAPI;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.EntityRobotBase;

public class AIRobotUnloadFluids extends AIRobot {
   private int waitedCycles;

   public AIRobotUnloadFluids(EntityRobotBase robot) {
      super(robot);
      this.setSuccess(false);
   }

   @Override
   public void update() {
      this.waitedCycles++;
      if (this.waitedCycles > 40) {
         if (unload(this.robot, this.robot.getDockingStation(), true) == 0) {
            this.terminate();
         } else {
            this.setSuccess(true);
            this.waitedCycles = 0;
         }
      }
   }

   public static int unload(EntityRobotBase robot, DockingStation station, boolean doUnload) {
      if (station == null || robot == null) {
         return 0;
      }

      return AIRobotLoadFluids.move(robot.getFluidStorage(), station.getFluidOutput(), null, doUnload);
   }

   @Override
   public long getPowerCost() {
      return MjAPI.MJ;
   }
}

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
import buildcraft.robotics.IStationFilter;

public class AIRobotRecharge extends AIRobot {
   public AIRobotRecharge(EntityRobotBase robot) {
      super(robot);
   }

   @Override
   public void start() {
      this.robot.releaseResources();
      this.robot.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
      this.startDelegateAI(new AIRobotSearchAndGotoStation(this.robot, new IStationFilter() {
         @Override
         public boolean matches(DockingStation station) {
            return station.providesPower();
         }
      }, null));
   }

   @Override
   public void delegateAIEnded(AIRobot ai) {
      if (ai instanceof AIRobotSearchAndGotoStation && !ai.success()) {
         this.setSuccess(false);
         this.terminate();
      }
   }

   @Override
   public void update() {
      if (this.robot.getBattery().getStored() >= EntityRobotBase.MAX_POWER - MjAPI.MJ * 500L) {
         this.terminate();
      }
   }

   @Override
   public long getPowerCost() {
      return 0L;
   }
}

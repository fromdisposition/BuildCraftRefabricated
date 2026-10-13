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
import buildcraft.robotics.BCRoboticsStatements;

public class AIRobotSleep extends AIRobot {
   // How long a robot rests at its station before re-checking for work comes from config; a short rest keeps
   // it responsive while still parking (and saving CPU) when there is nothing to do.
   private int sleptTime;

   public AIRobotSleep(EntityRobotBase robot) {
      super(robot);
   }

   @Override
   public void start() {
      this.robot.setItemActive(false);
   }

   @Override
   public void preempt(AIRobot ai) {
      // Checked every tick on purpose: a wakeup gate pulse can be active for a single tick, so sampling less often
      // would drop it. hasActiveAction is the allocation-free path (no per-tick action list).
      DockingStation linked = this.robot.getLinkedStation();
      if (linked != null && linked.hasActiveAction(BCRoboticsStatements.ACTION_ROBOT_WAKEUP.getUniqueTag())) {
         this.terminate();
      }
   }

   @Override
   public void update() {
      this.sleptTime++;
      if (this.sleptTime > buildcraft.robotics.BCRoboticsConfig.sleepSeconds.get() * 20) {
         this.terminate();
      }
   }

   @Override
   public boolean canLoadFromNBT() {
      return true;
   }

   @Override
   public long getPowerCost() {
      return this.sleptTime % 10 == 0 ? MjAPI.MJ / 10L : 0L;
   }
}

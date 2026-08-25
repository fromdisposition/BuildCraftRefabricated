/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.ai;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.path.PathFinding;
import net.minecraft.core.BlockPos;

/**
 * Out of power. The robot returns to its home station and parks there, because a docked robot is charged by the
 * station whatever its AI is doing -- so restoring power to that station revives it with no player action. A
 * robot that instead sank to the ground where its battery died could never dock again, and was unrecoverable
 * short of a wrench. Without a home station to return to there is nowhere to go, so it settles in place.
 */
public class AIRobotShutdown extends AIRobotGoto {
   private boolean parked;

   public AIRobotShutdown(EntityRobotBase robot) {
      super(robot);
   }

   @Override
   public void start() {
      this.robot.setItemActive(false);
      this.clearDestination(this.robot);

      DockingStation home = this.robot.getLinkedStation();
      if (home != null && this.robot.getDockingStation() != home) {
         this.startDelegateAI(new AIRobotGotoStation(this.robot, home));
      } else {
         this.parked = this.robot.getDockingStation() != null;
      }
   }

   @Override
   public void delegateAIEnded(AIRobot ai) {
      if (ai instanceof AIRobotGotoStation) {
         this.parked = ai.success();
      }
   }

   @Override
   public void update() {
      // Any charge at all means the station came back to life: hand control back so the main AI can decide
      // between recharging and working. Staying shut down would ignore a repaired supply forever.
      if (this.robot.getBattery().getStored() > EntityRobotBase.SHUTDOWN_POWER) {
         this.terminate();
         return;
      }

      if (this.parked) {
         return;
      }

      double feetY = this.robot.getY() - 0.1;
      BlockPos feet = BlockPos.containing(this.robot.getX(), feetY, this.robot.getZ());
      if (PathFinding.isSoftBlock(this.robot.level(), feet)) {
         this.robot.setPos(this.robot.getX(), this.robot.getY() - 0.075, this.robot.getZ());
      }
   }

   @Override
   public long getPowerCost() {
      return 0L;
   }
}

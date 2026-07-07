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
import buildcraft.api.statements.StatementSlot;
import buildcraft.robotics.BCRoboticsStatements;
import buildcraft.robotics.entity.EntityRobot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class AIRobotSleep extends AIRobot {
   // How long a robot rests at its station before re-checking for work. The classic 60s made robots feel dead --
   // a Knight only re-scanned for mobs once a minute, and a robot that missed its tool on the first try waited a
   // full minute to retry. 3s keeps them responsive while still parking (and saving CPU) when there is nothing to do.
   private static final int SLEEPING_TIME = 3 * 20;
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
      DockingStation linked = this.robot.getLinkedStation();
      if (linked == null) {
         return;
      }

      for (StatementSlot slot : linked.getActiveActions()) {
         if (slot.statement != null && BCRoboticsStatements.ACTION_ROBOT_WAKEUP.getUniqueTag().equals(slot.statement.getUniqueTag())) {
            this.terminate();
            return;
         }
      }
   }

   @Override
   public void update() {
      this.sleptTime++;

      DockingStation station = this.robot.getDockingStation();
      if (station != null && this.robot instanceof EntityRobot entityRobot) {
         BlockPos pos = station.getPos();
         entityRobot.destination = Vec3.atCenterOf(pos)
            .add(station.side().getStepX() * 0.5, station.side().getStepY() * 0.5, station.side().getStepZ() * 0.5);
      }

      if (this.sleptTime > SLEEPING_TIME) {
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

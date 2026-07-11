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
   // Waiting for a near-full battery is only correct while power actually flows in. If the pipe stops feeding the
   // station mid-charge (engine off, RF network empty), the robot used to sit docked in this AI forever, ignoring
   // all work until a world reload dropped the (non-persisted) AI. Slowest legit source is a pulsing redstone
   // engine (seconds between pulses), so 30s with zero gain is a safe "supply is dead" signal.
   private static final int STALL_TICKS = 30 * 20;

   private long lastStored = -1L;
   private int stallTicks;

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
      long stored = this.robot.getBattery().getStored();
      if (stored >= EntityRobotBase.MAX_POWER - MjAPI.MJ * 500L) {
         this.terminate();
         return;
      }

      if (stored > this.lastStored) {
         this.lastStored = stored;
         this.stallTicks = 0;
      } else if (++this.stallTicks > STALL_TICKS) {
         // Charging stalled: give up (AIRobotMain applies its recharge cooldown) so the robot goes back to work
         // on the power it has instead of sleeping forever; it will retry recharging later.
         this.setSuccess(false);
         this.terminate();
      }
   }

   @Override
   public long getPowerCost() {
      return 0L;
   }
}

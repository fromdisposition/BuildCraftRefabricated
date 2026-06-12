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
import buildcraft.lib.fabric.transfer.FluidVariants;
import buildcraft.robotics.path.IFluidFilter;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

public class AIRobotLoadFluids extends AIRobot {
   public static final int BUCKET_VOLUME = 1000;

   private IFluidFilter filter;
   private int waitedCycles;

   public AIRobotLoadFluids(EntityRobotBase robot) {
      super(robot);
   }

   public AIRobotLoadFluids(EntityRobotBase robot, IFluidFilter filter) {
      this(robot);
      this.filter = filter;
      this.setSuccess(false);
   }

   @Override
   public void update() {
      if (this.filter == null) {
         this.terminate();
         return;
      }

      this.waitedCycles++;
      if (this.waitedCycles > 40) {
         if (load(this.robot, this.robot.getDockingStation(), this.filter, true) == 0) {
            this.terminate();
         } else {
            this.setSuccess(true);
            this.waitedCycles = 0;
         }
      }
   }

   public static int load(EntityRobotBase robot, DockingStation station, IFluidFilter filter, boolean doLoad) {
      if (station == null || robot == null) {
         return 0;
      }

      return move(station.getFluidInput(), robot.getFluidStorage(), filter, doLoad);
   }

   static int move(Storage<FluidVariant> from, Storage<FluidVariant> to, IFluidFilter filter, boolean doMove) {
      if (from == null || to == null) {
         return 0;
      }

      try (Transaction transaction = Transaction.openOuter()) {
         long moved = StorageUtil.move(from, to, variant -> filter == null || filter.matches(variant.getFluid()), FluidVariants.mbToDroplets(BUCKET_VOLUME), transaction);
         if (moved > 0L && doMove) {
            transaction.commit();
         }

         return (int) FluidVariants.dropletsToMb(moved);
      }
   }

   @Override
   public long getPowerCost() {
      return MjAPI.MJ * 8L / 10L;
   }
}

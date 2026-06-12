/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.ai;

import buildcraft.api.mj.MjAPI;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.lib.fluid.stack.FluidStack;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.fabric.transfer.fluid.FluidStorageOps;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

public class AIRobotPumpBlock extends AIRobot {
   private BlockPos blockToPump;
   private long waited;
   private int pumped;

   public AIRobotPumpBlock(EntityRobotBase robot) {
      super(robot);
   }

   public AIRobotPumpBlock(EntityRobotBase robot, BlockPos blockToPump) {
      this(robot);
      this.blockToPump = blockToPump;
   }

   @Override
   public void start() {
      if (this.blockToPump == null) {
         this.terminate();
         return;
      }

      this.robot.aimItemAt(this.blockToPump);
   }

   @Override
   public void update() {
      if (this.blockToPump == null) {
         this.terminate();
         return;
      }

      if (this.waited < 40L) {
         this.waited++;
         return;
      }

      Level world = this.robot.level();
      FluidStack drainable = BlockUtil.drainBlock(world, this.blockToPump, false);
      if (drainable != null && !drainable.isEmpty()) {
         try (Transaction transaction = Transaction.openOuter()) {
            int inserted = FluidStorageOps.insertFluidMb(this.robot.getFluidStorage(), drainable, drainable.getAmount(), transaction);
            if (inserted >= drainable.getAmount()) {
               transaction.commit();
               BlockUtil.drainBlock(world, this.blockToPump, true);
               this.pumped += inserted;
            }
         }
      }

      this.terminate();
   }

   @Override
   public long getPowerCost() {
      return MjAPI.MJ / 2L;
   }

   @Override
   public boolean success() {
      return this.pumped > 0;
   }

   @Override
   public boolean canLoadFromNBT() {
      return true;
   }

   @Override
   public void writeSelfToNBT(CompoundTag nbt) {
      super.writeSelfToNBT(nbt);
      if (this.blockToPump != null) {
         nbt.putInt("x", this.blockToPump.getX());
         nbt.putInt("y", this.blockToPump.getY());
         nbt.putInt("z", this.blockToPump.getZ());
      }
   }

   @Override
   public void loadSelfFromNBT(CompoundTag nbt) {
      super.loadSelfFromNBT(nbt);
      if (nbt.contains("x")) {
         this.blockToPump = new BlockPos(nbt.getInt("x").orElse(0), nbt.getInt("y").orElse(0), nbt.getInt("z").orElse(0));
      }
   }
}

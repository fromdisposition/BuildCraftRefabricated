/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.ai;

import buildcraft.lib.nbt.BcNbt;
import buildcraft.api.robots.EntityRobotBase;
import java.util.LinkedList;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

/**
 * Fly the robot straight to a target block.
 *
 * Robots are {@code noPhysics} flyers, so a direct line always reaches the target. This deliberately replaces the
 * old voxel A* ({@code buildcraft.robotics.path.PathFinding}) which drove every robot move: that search frequently
 * returned no path (iteration budget, exact-block goal, over-pruned diagonals) and reported failure, which stranded
 * the robot -- unable to fly back to its station to recharge, or to reach a mob / work block -- so it just sat
 * there doing nothing. A straight flight can never "fail to find a route", only be interrupted, so navigation is
 * reliable. The precise final dock still uses {@link AIRobotStraightMoveTo}; this handles the long approach leg.
 */
public class AIRobotGotoBlock extends AIRobotGoto {
   /** Arrival tolerance (~0.6 block); the exact dock is done separately by AIRobotStraightMoveTo. */
   private static final double ARRIVED_SQ = 0.36;
   /** A noPhysics straight flight always closes distance, so no progress for this many ticks means truly stuck. */
   private static final int STUCK_TICKS = 100;

   private int finalX;
   private int finalY;
   private int finalZ;
   private double lastDistSq = Double.MAX_VALUE;
   private int noProgressTicks;

   public AIRobotGotoBlock(EntityRobotBase robot) {
      super(robot);
   }

   public AIRobotGotoBlock(EntityRobotBase robot, int x, int y, int z) {
      this(robot);
      this.finalX = x;
      this.finalY = y;
      this.finalZ = z;
   }

   /** maxDistance is legacy from the A* era ("arrive within N"); a straight flight goes to the block, so it is ignored. */
   public AIRobotGotoBlock(EntityRobotBase robot, int x, int y, int z, double maxDistance) {
      this(robot, x, y, z);
   }

   public AIRobotGotoBlock(EntityRobotBase robot, LinkedList<BlockPos> path) {
      this(robot);
      BlockPos last = path.getLast();
      this.finalX = last.getX();
      this.finalY = last.getY();
      this.finalZ = last.getZ();
   }

   @Override
   public void start() {
      this.robot.undock();
      this.aimAtTarget();
      this.setDestination(this.robot, this.finalX + 0.5, this.finalY + 0.5, this.finalZ + 0.5);
   }

   @Override
   public void update() {
      double dx = this.finalX + 0.5 - this.robot.getX();
      double dy = this.finalY + 0.5 - this.robot.getY();
      double dz = this.finalZ + 0.5 - this.robot.getZ();
      double distSq = dx * dx + dy * dy + dz * dz;

      if (distSq < ARRIVED_SQ) {
         this.robot.setPos(this.finalX + 0.5, this.finalY + 0.5, this.finalZ + 0.5);
         this.clearDestination(this.robot);
         this.terminate();
         return;
      }

      // Re-arm the flight every tick: moveTowardsDestination clears the destination once within 0.1 of it, and a
      // docking pass can clear it too, so keep pointing the robot at the target until it has actually arrived.
      this.setDestination(this.robot, this.finalX + 0.5, this.finalY + 0.5, this.finalZ + 0.5);
      this.aimAtTarget();

      if (distSq < this.lastDistSq - 1.0E-4) {
         this.lastDistSq = distSq;
         this.noProgressTicks = 0;
      } else if (++this.noProgressTicks > STUCK_TICKS) {
         this.setSuccess(false);
         this.terminate();
      }
   }

   private void aimAtTarget() {
      this.robot.aimItemAt(new BlockPos(this.finalX, this.finalY, this.finalZ));
   }

   @Override
   public void end() {
      this.clearDestination(this.robot);
   }

   @Override
   public boolean canLoadFromNBT() {
      return true;
   }

   @Override
   public void writeSelfToNBT(CompoundTag nbt) {
      super.writeSelfToNBT(nbt);
      nbt.putInt("finalX", this.finalX);
      nbt.putInt("finalY", this.finalY);
      nbt.putInt("finalZ", this.finalZ);
   }

   @Override
   public void loadSelfFromNBT(CompoundTag nbt) {
      super.loadSelfFromNBT(nbt);
      this.finalX = BcNbt.getInt(nbt, "finalX", 0);
      this.finalY = BcNbt.getInt(nbt, "finalY", 0);
      this.finalZ = BcNbt.getInt(nbt, "finalZ", 0);
   }
}

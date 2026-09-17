/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.ai;

import buildcraft.lib.nbt.BcNbt;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.entity.EntityRobot;
import buildcraft.robotics.path.PathFinding;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

/**
 * Flies to a block along a path that stays out of every block with a collision shape. The path is searched
 * incrementally over several ticks, then followed waypoint by waypoint; a flight that gets blocked by a change in
 * the world replans a few times before the target is reported unreachable and remembered as such.
 */
public class AIRobotGotoBlock extends AIRobotGoto {
   private static final double WAYPOINT_SQ = 0.04;
   private static final int STUCK_TICKS = 40;
   private static final int MAX_REPLANS = 4;

   private int finalX;
   private int finalY;
   private int finalZ;
   private double maxDistance;
   private PathFinding search;
   private List<BlockPos> path;
   private int nextWaypoint;
   private int replans;
   private double lastDistSq = Double.MAX_VALUE;
   private int noProgressTicks;

   public AIRobotGotoBlock(EntityRobotBase robot) {
      super(robot);
   }

   public AIRobotGotoBlock(EntityRobotBase robot, int x, int y, int z) {
      this(robot, x, y, z, 0.0);
   }

   public AIRobotGotoBlock(EntityRobotBase robot, int x, int y, int z, double maxDistance) {
      this(robot);
      this.finalX = x;
      this.finalY = y;
      this.finalZ = z;
      this.maxDistance = maxDistance;
   }

   @Override
   public void start() {
      this.robot.undock();
      this.robot.aimItemAt(this.target());
      this.plan();
   }

   @Override
   public void update() {
      if (this.search == null && this.path == null) {
         this.plan();
      }

      if (this.search != null) {
         this.search.iterate();
         if (!this.search.isDone()) {
            return;
         }

         this.path = this.search.getResult();
         this.search = null;
         if (this.path == null) {
            this.fail();
            return;
         }
      }

      Vec3 waypoint = PathFinding.feet(this.path.get(this.nextWaypoint));
      double distSq = this.robot.position().distanceToSqr(waypoint);
      if (distSq < WAYPOINT_SQ) {
         this.nextWaypoint++;
         if (this.nextWaypoint >= this.path.size()) {
            this.clearDestination(this.robot);
            this.setSuccess(true);
            this.terminate();
            return;
         }

         waypoint = PathFinding.feet(this.path.get(this.nextWaypoint));
         distSq = this.robot.position().distanceToSqr(waypoint);
         this.lastDistSq = Double.MAX_VALUE;
         this.noProgressTicks = 0;
      }

      if (this.robot instanceof EntityRobot entityRobot && entityRobot.isMovementBlocked()) {
         this.replan();
         return;
      }

      this.setDestination(this.robot, waypoint.x, waypoint.y, waypoint.z);
      if (distSq < this.lastDistSq - 1.0E-4) {
         this.lastDistSq = distSq;
         this.noProgressTicks = 0;
      } else if (++this.noProgressTicks > STUCK_TICKS) {
         this.replan();
      }
   }

   private void plan() {
      this.clearDestination(this.robot);
      this.path = null;
      this.nextWaypoint = 0;
      this.lastDistSq = Double.MAX_VALUE;
      this.noProgressTicks = 0;
      this.search = new PathFinding(this.robot.level(), BlockPos.containing(this.robot.position()), this.target(), this.maxDistance);
   }

   private void replan() {
      if (++this.replans > MAX_REPLANS) {
         this.fail();
      } else {
         this.plan();
      }
   }

   private void fail() {
      this.robot.unreachableBlockDetected(this.target());
      this.clearDestination(this.robot);
      this.setSuccess(false);
      this.terminate();
   }

   private BlockPos target() {
      return new BlockPos(this.finalX, this.finalY, this.finalZ);
   }

   @Override
   public void end() {
      this.search = null;
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
      nbt.putDouble("maxDistance", this.maxDistance);
   }

   @Override
   public void loadSelfFromNBT(CompoundTag nbt) {
      super.loadSelfFromNBT(nbt);
      this.finalX = BcNbt.getInt(nbt, "finalX", 0);
      this.finalY = BcNbt.getInt(nbt, "finalY", 0);
      this.finalZ = BcNbt.getInt(nbt, "finalZ", 0);
      this.maxDistance = BcNbt.getDouble(nbt, "maxDistance", 0.0);
   }
}

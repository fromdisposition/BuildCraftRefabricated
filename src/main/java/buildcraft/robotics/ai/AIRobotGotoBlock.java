/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.ai;

import buildcraft.lib.nbt.BcNbt;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.path.PathFinding;
import java.util.LinkedList;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.phys.Vec3;

public class AIRobotGotoBlock extends AIRobotGoto {
   private PathFinding pathSearch;
   private LinkedList<BlockPos> path;
   private int finalX;
   private int finalY;
   private int finalZ;
   private double maxDistance;
   private BlockPos lastBlockInPath;
   private boolean loadedFromNBT;

   public AIRobotGotoBlock(EntityRobotBase robot) {
      super(robot);
   }

   public AIRobotGotoBlock(EntityRobotBase robot, int x, int y, int z) {
      this(robot);
      this.finalX = x;
      this.finalY = y;
      this.finalZ = z;
   }

   public AIRobotGotoBlock(EntityRobotBase robot, int x, int y, int z, double maxDistance) {
      this(robot, x, y, z);
      this.maxDistance = maxDistance;
   }

   public AIRobotGotoBlock(EntityRobotBase robot, LinkedList<BlockPos> path) {
      this(robot);
      this.path = path;
      BlockPos last = path.getLast();
      this.finalX = last.getX();
      this.finalY = last.getY();
      this.finalZ = last.getZ();
      this.setNextInPath();
   }

   @Override
   public void start() {
      this.robot.undock();
   }

   @Override
   public void update() {
      if (this.loadedFromNBT) {
         this.setNextInPath();
         this.loadedFromNBT = false;
      }

      if (this.path == null && this.pathSearch == null) {
         BlockPos start = this.robot.blockPosition();
         BlockPos goal = new BlockPos(this.finalX, this.finalY, this.finalZ);
         this.pathSearch = new PathFinding(this.robot.level(), start, goal, this.maxDistance, 96.0F);
      } else if (this.path != null) {
         if (!this.robot.isMoving()) {
            if (!this.path.isEmpty()) {
               this.path.removeFirst();
            }

            this.setNextInPath();
         }
      } else {
         this.pathSearch.iterate(PathFinding.PATH_ITERATIONS);
         if (this.pathSearch.isDone()) {
            this.path = this.pathSearch.getResult();
            if (this.path.isEmpty()) {
               this.setSuccess(false);
               this.terminate();
               return;
            }

            this.lastBlockInPath = this.path.getLast();
            this.setNextInPath();
         }
      }

      if (this.path != null && this.path.isEmpty()) {
         this.clearDestination(this.robot);
         if (this.lastBlockInPath != null) {
            this.robot.setPos(this.lastBlockInPath.getX() + 0.5, this.lastBlockInPath.getY() + 0.5, this.lastBlockInPath.getZ() + 0.5);
         }

         this.terminate();
      }
   }

   private void setNextInPath() {
      if (this.path != null && !this.path.isEmpty()) {
         BlockPos next = this.path.getFirst();
         if (PathFinding.isSoftBlock(this.robot.level(), next) || next.equals(this.robot.blockPosition())) {
            this.setDestination(this.robot, next.getX() + 0.5, next.getY() + 0.5, next.getZ() + 0.5);
            this.robot.aimItemAt(next);
         } else {
            this.path = null;
            this.pathSearch = null;
            this.clearDestination(this.robot);
         }
      }
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
      nbt.putDouble("maxDistance", this.maxDistance);
      if (this.path != null) {
         ListTag pathList = new ListTag();
         for (BlockPos pos : this.path) {
            CompoundTag sub = new CompoundTag();
            sub.putInt("x", pos.getX());
            sub.putInt("y", pos.getY());
            sub.putInt("z", pos.getZ());
            pathList.add(sub);
         }

         nbt.put("path", pathList);
      }
   }

   @Override
   public void loadSelfFromNBT(CompoundTag nbt) {
      super.loadSelfFromNBT(nbt);
      this.finalX = BcNbt.getInt(nbt, "finalX", 0);
      this.finalY = BcNbt.getInt(nbt, "finalY", 0);
      this.finalZ = BcNbt.getInt(nbt, "finalZ", 0);
      this.maxDistance = BcNbt.getDouble(nbt, "maxDistance", 0.0);
      if (nbt.contains("path")) {
         ListTag pathList = BcNbt.getList(nbt, "path");
         this.path = new LinkedList<>();
         for (int i = 0; i < pathList.size(); i++) {
            if (pathList.get(i) instanceof CompoundTag sub) {
               this.path.add(new BlockPos(BcNbt.getInt(sub, "x", 0), BcNbt.getInt(sub, "y", 0), BcNbt.getInt(sub, "z", 0)));
            }
         }
      }

      this.loadedFromNBT = true;
   }
}

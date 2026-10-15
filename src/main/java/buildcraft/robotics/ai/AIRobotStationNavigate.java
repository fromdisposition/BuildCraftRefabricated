/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.ai;

import buildcraft.lib.nbt.BcNbt;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.path.FlightSweep;
import buildcraft.robotics.path.PathFinding;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Flies to a docking station in two legs: a pathfound flight to an approach cell next to the station, then the
 * straight dock onto its face. The approach cell is the one in front of the station when it is free; a station
 * facing an inventory is approached from a neighbouring free cell that has a clear straight line onto the face.
 */
public abstract class AIRobotStationNavigate extends AIRobot {
   protected BlockPos stationIndex;
   protected Direction stationSide;

   protected AIRobotStationNavigate(EntityRobotBase robot) {
      super(robot);
   }

   protected void beginNavigation(DockingStation station) {
      this.stationIndex = station.index();
      this.stationSide = station.side();
      if (this.robot.isKnownUnreachable(station.index().relative(station.side()))) {
         this.terminate();
         return;
      }

      BlockPos approach = this.chooseApproach(station);
      if (approach == null) {
         this.robot.unreachableBlockDetected(station.index().relative(station.side()));
         this.terminate();
         return;
      }

      this.startDelegateAI(new AIRobotGotoBlock(this.robot, approach.getX(), approach.getY(), approach.getZ()));
   }

   private BlockPos chooseApproach(DockingStation station) {
      Level level = this.robot.level();
      BlockPos index = station.index();
      BlockPos front = index.relative(station.side());
      if (PathFinding.isSoftBlock(level, front)) {
         return front;
      }

      Vec3 dock = dockPosition(station);
      Vec3 here = this.robot.position();
      BlockPos best = null;
      double bestDistSq = Double.MAX_VALUE;
      for (int dx = -1; dx <= 1; dx++) {
         for (int dy = -1; dy <= 1; dy++) {
            for (int dz = -1; dz <= 1; dz++) {
               if (dx == 0 && dy == 0 && dz == 0) {
                  continue;
               }

               BlockPos cell = front.offset(dx, dy, dz);
               if (cell.equals(index) || !PathFinding.isSoftBlock(level, cell)) {
                  continue;
               }

               double distSq = here.distanceToSqr(PathFinding.feet(cell));
               if (distSq < bestDistSq && FlightSweep.isClear(level, PathFinding.feet(cell), dock, index, front)) {
                  best = cell;
                  bestDistSq = distSq;
               }
            }
         }
      }

      return best;
   }

   private static Vec3 dockPosition(DockingStation station) {
      Direction side = station.side();
      BlockPos index = station.index();
      return new Vec3(index.getX() + 0.5 + side.getStepX() * 0.5, index.getY() + 0.5 + side.getStepY() * 0.5, index.getZ() + 0.5 + side.getStepZ() * 0.5);
   }

   protected void beginStraightDock(DockingStation station) {
      Vec3 dock = dockPosition(station);
      this.startDelegateAI(new AIRobotStraightMoveTo(this.robot, dock.x, dock.y, dock.z, station.index(), station.index().relative(station.side())));
   }

   @Override
   public void delegateAIEnded(AIRobot ai) {
      DockingStation station = this.getStation();
      if (station == null) {
         this.terminate();
      } else if (ai instanceof AIRobotGotoBlock) {
         if (ai.success()) {
            this.beginStraightDock(station);
         } else {
            this.robot.unreachableBlockDetected(station.index().relative(station.side()));
            this.terminate();
         }
      } else if (ai instanceof AIRobotStraightMoveTo) {
         if (!ai.success()) {
            this.terminate();
         } else {
            this.onReachedDock(station);
         }
      } else if (!ai.success()) {
         this.terminate();
      }
   }

   protected abstract void onReachedDock(DockingStation station);

   protected DockingStation getStation() {
      return this.stationIndex == null ? null : this.robot.getRegistry().getStation(this.stationIndex, this.stationSide);
   }

   @Override
   public boolean canLoadFromNBT() {
      return true;
   }

   @Override
   public void writeSelfToNBT(CompoundTag nbt) {
      super.writeSelfToNBT(nbt);
      if (this.stationIndex != null) {
         nbt.putIntArray("stationIndex", new int[]{this.stationIndex.getX(), this.stationIndex.getY(), this.stationIndex.getZ()});
         nbt.putByte("stationSide", (byte)this.stationSide.ordinal());
      }
   }

   @Override
   public void loadSelfFromNBT(CompoundTag nbt) {
      super.loadSelfFromNBT(nbt);
      int[] arr = BcNbt.getIntArray(nbt, "stationIndex");
      if (arr.length == 3) {
         this.stationIndex = new BlockPos(arr[0], arr[1], arr[2]);
         this.stationSide = Direction.values()[BcNbt.getByte(nbt, "stationSide", (byte)0)];
      }
   }
}

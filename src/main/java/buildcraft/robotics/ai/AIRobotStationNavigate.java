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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

public abstract class AIRobotStationNavigate extends AIRobot {
   protected BlockPos stationIndex;
   protected Direction stationSide;

   protected AIRobotStationNavigate(EntityRobotBase robot) {
      super(robot);
   }

   protected void beginNavigation(DockingStation station) {
      this.stationIndex = station.index();
      this.stationSide = station.side();
      Direction side = station.side();
      int steps = this.approachSteps();
      BlockPos index = station.index();
      this.startDelegateAI(
         new AIRobotGotoBlock(
            this.robot,
            index.getX() + side.getStepX() * steps,
            index.getY() + side.getStepY() * steps,
            index.getZ() + side.getStepZ() * steps
         )
      );
   }

   protected void beginStraightDock(DockingStation station) {
      Direction side = station.side();
      BlockPos index = station.index();
      this.startDelegateAI(
         new AIRobotStraightMoveTo(
            this.robot,
            index.getX() + 0.5 + side.getStepX() * 0.5,
            index.getY() + 0.5 + side.getStepY() * 0.5,
            index.getZ() + 0.5 + side.getStepZ() * 0.5
         )
      );
   }

   protected abstract int approachSteps();

   @Override
   public void delegateAIEnded(AIRobot ai) {
      DockingStation station = this.getStation();
      if (station == null) {
         this.terminate();
      } else if (ai instanceof AIRobotGotoBlock) {
         if (ai.success()) {
            this.beginStraightDock(station);
         } else {
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

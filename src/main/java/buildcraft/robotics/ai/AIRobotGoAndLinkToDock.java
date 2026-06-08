/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.ai;

import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.EntityRobotBase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

public class AIRobotGoAndLinkToDock extends AIRobotStationNavigate {
   private DockingStation station;

   public AIRobotGoAndLinkToDock(EntityRobotBase robot) {
      super(robot);
   }

   public AIRobotGoAndLinkToDock(EntityRobotBase robot, DockingStation station) {
      super(robot);
      this.station = station;
      if (station != null) {
         this.stationIndex = station.index();
         this.stationSide = station.side();
      }
   }

   @Override
   public void start() {
      if (this.station == this.robot.getLinkedStation() && this.station == this.robot.getDockingStation()) {
         this.terminate();
      } else if (this.station != null && this.station.takeAsMain(this.robot)) {
         this.beginNavigation(this.station);
      } else {
         this.setSuccess(false);
         this.terminate();
      }
   }

   @Override
   protected int approachSteps() {
      return 2;
   }

   @Override
   protected void onReachedDock(DockingStation station) {
      this.robot.dock(station);
      this.terminate();
   }

   @Override
   protected DockingStation getStation() {
      return this.station != null ? this.station : super.getStation();
   }

   @Override
   public void loadSelfFromNBT(CompoundTag nbt) {
      super.loadSelfFromNBT(nbt);
      int[] arr = nbt.getIntArray("stationIndex").orElse(new int[0]);
      if (arr.length == 3) {
         BlockPos index = new BlockPos(arr[0], arr[1], arr[2]);
         Direction side = Direction.values()[nbt.getByte("stationSide").orElse((byte)0)];
         this.station = this.robot.getRegistry().getStation(index, side);
      } else {
         this.station = this.robot.getLinkedStation();
      }
   }
}

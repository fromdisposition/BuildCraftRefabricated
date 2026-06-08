/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.ai;

import buildcraft.api.core.IZone;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.IStationFilter;
import buildcraft.robotics.statement.StationActions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class AIRobotSearchStation extends AIRobot {
   public DockingStation targetStation;
   private IStationFilter filter;
   private IZone zone;

   public AIRobotSearchStation(EntityRobotBase robot) {
      super(robot);
   }

   public AIRobotSearchStation(EntityRobotBase robot, IStationFilter filter, IZone zone) {
      this(robot);
      this.filter = filter;
      this.zone = zone;
   }

   @Override
   public void start() {
      if (this.robot.getDockingStation() != null && this.filter.matches(this.robot.getDockingStation())) {
         this.targetStation = this.robot.getDockingStation();
         this.terminate();
         return;
      }

      double bestDistance = Double.MAX_VALUE;
      DockingStation best = null;

      for (DockingStation station : this.robot.getRegistry().getStations()) {
         if (!station.isInitialized()) {
            continue;
         }

         if (station.isTaken() && station.robotIdTaking() != this.robot.getRobotId()) {
            continue;
         }

         BlockPos pos = station.index();
         if (this.zone != null && !this.zone.contains(Vec3.atCenterOf(pos))) {
            continue;
         }

         if (this.filter.matches(station) && !StationActions.isRobotForbidden(station, this.robot)) {
            double distance = this.robot.position().distanceToSqr(Vec3.atCenterOf(pos));
            if (best == null || distance < bestDistance) {
               best = station;
               bestDistance = distance;
            }
         }
      }

      if (best != null) {
         this.targetStation = best;
      }

      this.terminate();
   }

   @Override
   public void delegateAIEnded(AIRobot ai) {
      this.terminate();
   }

   @Override
   public boolean success() {
      return this.targetStation != null;
   }
}

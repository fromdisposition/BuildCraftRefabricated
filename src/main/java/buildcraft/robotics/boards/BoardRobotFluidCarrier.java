/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.boards;

import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.ai.StationErrand;
import buildcraft.robotics.statement.StationActions;

public class BoardRobotFluidCarrier extends BoardRobotGenericHauler {
   public BoardRobotFluidCarrier(EntityRobotBase robot) {
      super(robot);
   }

   @Override
   public RedstoneBoardRobotNBT getNBTHandler() {
      return BCBoardNBT.REGISTRY.get("fluidCarrier");
   }

   @Override
   protected boolean carrying() {
      return this.robot.hasFluid();
   }

   @Override
   protected StationErrand loadErrand() {
      return StationErrand.loadFluids(StationActions.getGateFluidFilter(this.robot.getLinkedStation()));
   }

   @Override
   protected StationErrand unloadErrand() {
      return StationErrand.unloadFluids();
   }
}

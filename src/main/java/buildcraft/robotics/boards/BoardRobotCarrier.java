/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.boards;

import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.ai.AIRobotLoad;
import buildcraft.robotics.ai.StationErrand;
import buildcraft.robotics.statement.StationActions;

public class BoardRobotCarrier extends BoardRobotGenericHauler {
   public BoardRobotCarrier(EntityRobotBase robot) {
      super(robot);
   }

   @Override
   public RedstoneBoardRobotNBT getNBTHandler() {
      return BCBoardNBT.REGISTRY.get("carrier");
   }

   @Override
   protected boolean carrying() {
      return this.robot.containsItems();
   }

   @Override
   protected StationErrand loadErrand() {
      return StationErrand.loadItems(StationActions.getGateFilter(this.robot.getLinkedStation()), AIRobotLoad.ANY_QUANTITY);
   }

   @Override
   protected StationErrand unloadErrand() {
      return StationErrand.unloadItems();
   }
}

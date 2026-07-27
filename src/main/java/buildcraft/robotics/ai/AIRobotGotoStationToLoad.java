/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.ai;

import buildcraft.api.core.IStackFilter;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.IStationFilter;

public class AIRobotGotoStationToLoad extends AIRobotGotoStationForInventory {
   private IStackFilter filter;
   private int quantity;

   public AIRobotGotoStationToLoad(EntityRobotBase robot) {
      super(robot, true);
   }

   public AIRobotGotoStationToLoad(EntityRobotBase robot, IStackFilter filter, int quantity) {
      super(robot, true);
      this.filter = filter;
      this.quantity = quantity;
   }

   @Override
   protected IStationFilter createStationFilter() {
      return station -> AIRobotLoad.load(this.robot, station, this.filter, this.quantity, false);
   }
}

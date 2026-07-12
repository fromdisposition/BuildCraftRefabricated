/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.ai;

import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.IStationFilter;
import buildcraft.robotics.path.IFluidFilter;

public class AIRobotGotoStationToLoadFluids extends AIRobotGotoStationForInventory {
   private IFluidFilter filter;

   public AIRobotGotoStationToLoadFluids(EntityRobotBase robot) {
      super(robot, true);
   }

   public AIRobotGotoStationToLoadFluids(EntityRobotBase robot, IFluidFilter filter) {
      super(robot, true);
      this.filter = filter;
   }

   @Override
   protected IStationFilter createStationFilter() {
      return station -> AIRobotLoadFluids.load(this.robot, station, this.filter, false) > 0;
   }
}

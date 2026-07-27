/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.ai;

import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.IStationFilter;

public class AIRobotGotoStationToUnload extends AIRobotGotoStationForInventory {
   public AIRobotGotoStationToUnload(EntityRobotBase robot) {
      super(robot, false);
   }

   @Override
   protected IStationFilter createStationFilter() {
      return station -> AIRobotUnload.unload(this.robot, station, false);
   }
}

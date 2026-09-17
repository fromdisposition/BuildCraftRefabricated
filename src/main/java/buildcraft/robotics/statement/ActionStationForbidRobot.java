/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.statement;

import buildcraft.robotics.BCRoboticsSprites;

public class ActionStationForbidRobot extends ActionStationRobot {
   private final boolean invert;

   public ActionStationForbidRobot(boolean invert) {
      super(
         invert ? "buildcraft:station.force_robot" : "buildcraft:station.forbid_robot",
         "gate.action.station." + (invert ? "force" : "forbid") + "_robot",
         invert ? BCRoboticsSprites.ACTION_STATION_FORCE_ROBOT : BCRoboticsSprites.ACTION_STATION_FORBID_ROBOT,
         3
      );
      this.invert = invert;
   }

   public boolean isInvert() {
      return this.invert;
   }
}

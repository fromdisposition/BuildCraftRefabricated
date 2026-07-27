/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.boards;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;

public abstract class RedstoneBoardRobot extends AIRobot implements IRedstoneBoard<EntityRobotBase> {
   public RedstoneBoardRobot(EntityRobotBase iRobot) {
      super(iRobot);
   }

   public abstract RedstoneBoardRobotNBT getNBTHandler();

   public final void updateBoard(EntityRobotBase container) {
   }

   @Override
   public boolean canLoadFromNBT() {
      return true;
   }
}

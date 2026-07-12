/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.boards;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.ai.AIRobotSleep;

public abstract class BoardRobotBC extends RedstoneBoardRobot {
   public BoardRobotBC(EntityRobotBase robot) {
      super(robot);
   }

   protected abstract String boardName();

   @Override
   public RedstoneBoardRobotNBT getNBTHandler() {
      return BCBoardNBT.REGISTRY.get(this.boardName());
   }

   @Override
   public void update() {
      this.startDelegateAI(new AIRobotSleep(this.robot));
   }
}

/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.ai;

import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.path.PathFinding;
import net.minecraft.core.BlockPos;

public class AIRobotShutdown extends AIRobotGoto {
   public AIRobotShutdown(EntityRobotBase robot) {
      super(robot);
   }

   @Override
   public void start() {
      this.robot.undock();
      this.robot.setItemActive(false);
      this.clearDestination(this.robot);
   }

   @Override
   public void update() {
      double feetY = this.robot.getY() - 0.1;
      BlockPos feet = BlockPos.containing(this.robot.getX(), feetY, this.robot.getZ());
      if (PathFinding.isSoftBlock(this.robot.level(), feet)) {
         this.robot.setPos(this.robot.getX(), this.robot.getY() - 0.075, this.robot.getZ());
      }
   }

   @Override
   public long getPowerCost() {
      return 0L;
   }
}

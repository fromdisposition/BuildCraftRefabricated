/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.ai;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.entity.EntityRobot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public abstract class AIRobotGoto extends AIRobot {
   public AIRobotGoto(EntityRobotBase robot) {
      super(robot);
   }

   protected void setDestination(EntityRobotBase robot, double x, double y, double z) {
      this.setDestination(robot, x, y, z, null, null);
   }

   protected void setDestination(EntityRobotBase robot, double x, double y, double z, BlockPos passThroughMin, BlockPos passThroughMax) {
      if (robot instanceof EntityRobot entityRobot) {
         entityRobot.setDestination(new Vec3(x, y, z), passThroughMin, passThroughMax);
      }
   }

   protected void clearDestination(EntityRobotBase robot) {
      if (robot instanceof EntityRobot entityRobot) {
         entityRobot.clearDestination();
      }
   }
}

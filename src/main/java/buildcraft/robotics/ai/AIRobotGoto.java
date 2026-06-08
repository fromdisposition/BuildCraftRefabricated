/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.ai;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.entity.EntityRobot;
import net.minecraft.world.phys.Vec3;

public abstract class AIRobotGoto extends AIRobot {
   public AIRobotGoto(EntityRobotBase robot) {
      super(robot);
   }

   protected void setDestination(EntityRobotBase robot, double x, double y, double z) {
      if (robot instanceof EntityRobot entityRobot) {
         entityRobot.destination = new Vec3(x, y, z);
      }
   }

   protected void clearDestination(EntityRobotBase robot) {
      if (robot instanceof EntityRobot entityRobot) {
         entityRobot.destination = null;
         entityRobot.setDeltaMovement(Vec3.ZERO);
      }
   }
}

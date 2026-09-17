/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics;

import buildcraft.fabric.BCRegistries;
import buildcraft.robotics.entity.EntityRobot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType.Builder;

public final class BCRoboticsEntities {
   public static EntityType<EntityRobot> ROBOT;

   private BCRoboticsEntities() {
   }

   public static void register() {
      ROBOT = BCRegistries.registerEntityType(
         "buildcraftrobotics",
         "robot",
         Builder.of(EntityRobot::new, MobCategory.MISC).sized(0.5F, 0.5F).clientTrackingRange(8).updateInterval(3)
      );
   }
}

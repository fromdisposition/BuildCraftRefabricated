/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.boards;

import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.robots.EntityRobotBase;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;

public class BoardRobotButcher extends BoardRobotGenericAttack {
   public BoardRobotButcher(EntityRobotBase robot) {
      super(robot);
   }

   @Override
   public RedstoneBoardRobotNBT getNBTHandler() {
      return BCBoardNBT.REGISTRY.get("butcher");
   }

   /** Adults only, and never a tamed animal: culling the young stops the farm breeding back, and a player's pet
    *  is not livestock. */
   @Override
   protected boolean isTarget(Entity entity) {
      if (!(entity instanceof Animal animal) || animal.isBaby()) {
         return false;
      }

      return !(entity instanceof TamableAnimal tamable) || !tamable.isTame();
   }
}

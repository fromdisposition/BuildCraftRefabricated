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
//? if >= 1.21.11 {
import net.minecraft.world.entity.animal.equine.AbstractHorse;
//?} else {
/*import net.minecraft.world.entity.animal.horse.AbstractHorse;
*///?}

public class BoardRobotButcher extends BoardRobotGenericAttack {
   public BoardRobotButcher(EntityRobotBase robot) {
      super(robot);
   }

   @Override
   public RedstoneBoardRobotNBT getNBTHandler() {
      return BCBoardNBT.REGISTRY.get("butcher");
   }

   /** Adults only, and never a player's animal (tamed, a tamed horse/llama or name-tagged): culling the young
    *  stops the farm breeding back, and a pet is not livestock. */
   @Override
   protected boolean isTarget(Entity entity) {
      if (!(entity instanceof Animal animal) || animal.isBaby() || animal.hasCustomName()) {
         return false;
      }

      if (entity instanceof TamableAnimal tamable && tamable.isTame()) {
         return false;
      }

      return !(entity instanceof AbstractHorse horse) || !horse.isTamed();
   }
}

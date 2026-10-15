/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.boards;

import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.robots.EntityRobotBase;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.monster.Enemy;

public class BoardRobotKnight extends BoardRobotGenericAttack {
   public BoardRobotKnight(EntityRobotBase robot) {
      super(robot);
   }

   @Override
   public RedstoneBoardRobotNBT getNBTHandler() {
      return BCBoardNBT.REGISTRY.get("knight");
   }

   @Override
   protected boolean isTarget(Entity entity) {
      return entity instanceof Enemy || (entity instanceof NeutralMob neutral && neutral.isAngry());
   }
}

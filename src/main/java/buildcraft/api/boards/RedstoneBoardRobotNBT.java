/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.boards;

import buildcraft.api.robots.EntityRobotBase;
import net.minecraft.nbt.CompoundTag;

public abstract class RedstoneBoardRobotNBT extends RedstoneBoardNBT<EntityRobotBase> {
   public RedstoneBoardRobot create(CompoundTag nbt, EntityRobotBase robot) {
      return this.create(robot);
   }

   public abstract RedstoneBoardRobot create(EntityRobotBase var1);

   public abstract Object getRobotTexture();
}

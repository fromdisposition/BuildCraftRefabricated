/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.boards;

import java.util.Collection;
import net.minecraft.nbt.CompoundTag;

public abstract class RedstoneBoardRegistry {
   public static RedstoneBoardRegistry instance;

   public abstract void registerBoardType(RedstoneBoardNBT<?> var1, long var2);

   public abstract void setEmptyRobotBoard(RedstoneBoardRobotNBT var1);

   public abstract RedstoneBoardRobotNBT getEmptyRobotBoard();

   public abstract RedstoneBoardNBT<?> getRedstoneBoard(CompoundTag var1);

   public abstract RedstoneBoardNBT<?> getRedstoneBoard(String var1);

   public abstract Collection<RedstoneBoardNBT<?>> getAllBoardNBTs();

   public abstract long getPowerCost(RedstoneBoardNBT<?> var1);
}

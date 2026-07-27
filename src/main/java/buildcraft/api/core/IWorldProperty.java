/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.core;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface IWorldProperty {
   boolean get(Level var1, BlockPos var2);

   void clear();
}

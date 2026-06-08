/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.cache;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public interface ITileCache {
   void invalidate();

   @Nullable
   TileCacheRet getTile(BlockPos var1);

   @Nullable
   TileCacheRet getTile(Direction var1);

   enum TileCacheState {
      CACHED,
      NOT_CACHED,
      NOT_PRESENT;
   }
}

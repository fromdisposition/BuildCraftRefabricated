/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.cache;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.LevelChunk;

public interface IChunkCache {
   void invalidate();

   @Nullable
   LevelChunk getChunk(BlockPos var1);

   enum ChunkCacheState {
      CACHED,
      NOT_CACHED,
      LOADED,
      NOT_LOADED;
   }
}

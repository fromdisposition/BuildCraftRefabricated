/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.chunkload;

import buildcraft.lib.BCLibConfig;
import buildcraft.lib.misc.PositionUtil;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class ChunkLoaderManager {
   private ChunkLoaderManager() {
   }

   public static <T extends BlockEntity & IChunkLoadingTile> void loadChunksForTile(T tile) {
      if (!canLoadFor(tile)) {
         releaseChunksFor(tile);
      } else {
         forceChunks(tile, true);
      }
   }

   public static <T extends BlockEntity & IChunkLoadingTile> void releaseChunksFor(T tile) {
      forceChunks(tile, false);
   }

   private static <T extends BlockEntity & IChunkLoadingTile> void forceChunks(T tile, boolean add) {
      if (tile.getLevel() instanceof ServerLevel serverLevel) {
         for (ChunkPos chunk : getChunksToLoad(tile)) {
            //? if >= 26.1 {
            serverLevel.setChunkForced(chunk.x(), chunk.z(), add);
            //?} else {
            /*serverLevel.setChunkForced(chunk.x, chunk.z, add);
            *///?}
         }
      }
   }

   public static <T extends BlockEntity & IChunkLoadingTile> Set<ChunkPos> getChunksToLoad(T tile) {
      Set<ChunkPos> chunksToLoad = tile.getChunksToLoad();
      Set<ChunkPos> chunkPoses = new HashSet<>(chunksToLoad != null ? chunksToLoad : Collections.emptyList());
      chunkPoses.add(PositionUtil.chunkContaining(tile.getBlockPos()));
      return chunkPoses;
   }

   private static boolean canLoadFor(IChunkLoadingTile tile) {
      return BCLibConfig.chunkLoadingLevel.get().canLoad(tile.getLoadType());
   }
}

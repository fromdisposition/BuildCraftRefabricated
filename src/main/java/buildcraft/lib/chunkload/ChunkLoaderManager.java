package buildcraft.lib.chunkload;

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
            serverLevel.setChunkForced(chunk.x(), chunk.z(), add);
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
      return tile.getLoadType() != null;
   }
}

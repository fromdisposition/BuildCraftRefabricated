package buildcraft.energy.generation.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.world.level.ChunkPos;

/**
 * Thread-local caches for oil worldgen. Chunk decoration runs on a worker pool; each thread keeps its own map so
 * neighbouring chunks do not re-roll the same origin chunk hundreds of times.
 */
final class OilGenCaches {
   private static final int MAX_STRUCTURE_ENTRIES = 8192;
   private static final ThreadLocal<StructureCache> STRUCTURES = ThreadLocal.withInitial(StructureCache::new);
   private static volatile int cacheGeneration;

   private OilGenCaches() {
   }

   static List<OilGenStructure> structure(
      long worldSeed, int chunkX, int chunkZ, int cacheScope, Supplier<List<OilGenStructure>> compute
   ) {
      return STRUCTURES.get().get(worldSeed, chunkX, chunkZ, cacheScope, compute);
   }

   static int cacheScope(OilGenSettings config) {
      return config.forcedTier().ordinal() << 1 | (config.useDatapackSpawnChance() ? 1 : 0);
   }

   static void invalidateAll() {
      cacheGeneration++;
      OilGenSettings.invalidate();
   }

   private static final class StructureCache {
      private long worldSeed = Long.MIN_VALUE;
      private int generation = -1;
      private final Map<Long, List<OilGenStructure>> byChunk = new HashMap<>();

      List<OilGenStructure> get(
         long worldSeed, int chunkX, int chunkZ, int cacheScope, Supplier<List<OilGenStructure>> compute
      ) {
         int gen = cacheGeneration;
         if (this.generation != gen) {
            this.byChunk.clear();
            this.generation = gen;
            this.worldSeed = Long.MIN_VALUE;
         }
         if (this.worldSeed != worldSeed) {
            this.byChunk.clear();
            this.worldSeed = worldSeed;
         }

         long key = packKey(chunkX, chunkZ, cacheScope);
         List<OilGenStructure> cached = this.byChunk.get(key);
         if (cached != null) {
            // #region agent log
            if (cached.isEmpty() && Math.abs(chunkX) <= 2 && Math.abs(chunkZ) <= 2) {
               OilGenDebugLog.log(
                  "H17",
                  "OilGenCaches.get",
                  "cache_hit_empty",
                  java.util.Map.of("originChunkX", chunkX, "originChunkZ", chunkZ, "cacheScope", cacheScope, "runId", "post-fix2")
               );
            }
            // #endregion
            return cached;
         }

         if (this.byChunk.size() >= MAX_STRUCTURE_ENTRIES) {
            this.byChunk.clear();
         }

         List<OilGenStructure> computed = compute.get();
         if (computed.isEmpty()) {
            cached = List.of();
         } else {
            cached = List.copyOf(computed);
         }
         // #region agent log
         if (!computed.isEmpty() && Math.abs(chunkX) <= 2 && Math.abs(chunkZ) <= 2) {
            OilGenDebugLog.log(
               "H17",
               "OilGenCaches.get",
               "cache_miss_computed",
               java.util.Map.of(
                  "originChunkX", chunkX,
                  "originChunkZ", chunkZ,
                  "cacheScope", cacheScope,
                  "structureCount", computed.size(),
                  "runId", "post-fix2"
               )
            );
         }
         // #endregion
         this.byChunk.put(key, cached);
         return cached;
      }

      private static long packKey(int chunkX, int chunkZ, int cacheScope) {
         return ChunkPos.pack(chunkX, chunkZ) ^ ((long) cacheScope << 60);
      }

   }
}

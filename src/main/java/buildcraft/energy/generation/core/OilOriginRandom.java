package buildcraft.energy.generation.core;

import net.minecraft.util.RandomSource;

/** Deterministic per-origin-chunk RNG for procedural oil geometry during scan stitching. */
final class OilOriginRandom {
   private static final long SALT = 0xD046B4E40C7D07CFL;
   private final RandomSource random;

   private OilOriginRandom(RandomSource random) {
      this.random = random;
   }

   static OilOriginRandom forOriginChunk(long worldSeed, int chunkX, int chunkZ) {
      long seed = worldSeed;
      seed = seed * 31L + chunkX;
      seed = seed * 31L + chunkZ;
      seed ^= SALT;
      return new OilOriginRandom(RandomSource.create(seed));
   }

   int nextInt(int bound) {
      return this.random.nextInt(bound);
   }

   double nextDouble() {
      return this.random.nextDouble();
   }

   float nextFloat() {
      return this.random.nextFloat();
   }
}

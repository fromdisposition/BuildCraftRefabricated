package buildcraft.energy.generation;

import buildcraft.lib.misc.RandUtil;
import java.util.Random;

/**
 * Deterministic per-origin-chunk RNG for oil worldgen.
 *
 * <p>Delegates to BC {@link RandUtil#createRandomForChunk} so world seeds stay compatible with BC 8.0.
 * This is the only supported entry point for oil origin-chunk rolls.
 */
final class BcChunkRandom {
   /** BC 8.0 oil generator salt ({@code MAGIC_GEN_NUMBER}). */
   static final long OIL_ORIGIN_CHUNK_SALT = 0xD0_46_B4_E4_0C_7D_07_CFL;

   private final Random delegate;

   private BcChunkRandom(Random delegate) {
      this.delegate = delegate;
   }

   static BcChunkRandom forOilOriginChunk(long worldSeed, int chunkX, int chunkZ) {
      return new BcChunkRandom(RandUtil.createRandomForChunk(worldSeed, chunkX, chunkZ, OIL_ORIGIN_CHUNK_SALT));
   }

   int nextInt(int bound) {
      return this.delegate.nextInt(bound);
   }

   double nextDouble() {
      return this.delegate.nextDouble();
   }

   float nextFloat() {
      return this.delegate.nextFloat();
   }

   static {
      verifyRandUtilParity();
   }

   private static void verifyRandUtilParity() {
      for (int i = 0; i < 128; i++) {
         long worldSeed = i * 0x9E3779B97F4A7C15L;
         int chunkX = i - 64;
         int chunkZ = i * 3;
         Random expected = RandUtil.createRandomForChunk(worldSeed, chunkX, chunkZ, OIL_ORIGIN_CHUNK_SALT);
         BcChunkRandom actual = forOilOriginChunk(worldSeed, chunkX, chunkZ);
         for (int roll = 0; roll < 32; roll++) {
            if (actual.nextInt(10_000) != expected.nextInt(10_000)
               || Double.compare(actual.nextDouble(), expected.nextDouble()) != 0
               || Float.compare(actual.nextFloat(), expected.nextFloat()) != 0) {
               throw new ExceptionInInitializerError("BcChunkRandom diverged from RandUtil for seed=" + worldSeed + " chunk=" + chunkX + "," + chunkZ);
            }
         }
      }
   }
}

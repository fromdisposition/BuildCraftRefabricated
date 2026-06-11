package buildcraft.lib.misc;

import java.util.Random;

/** Utilities based around common deterministic chunk RNG usage. */
public final class RandUtil {
   private RandUtil() {
   }

   public static Random createRandomForChunk(long worldSeed, int chunkX, int chunkZ, long magicNumber) {
      Random worldRandom = new Random(worldSeed);
      long xSeed = (worldRandom.nextLong() >> 2) + 1L;
      long zSeed = (worldRandom.nextLong() >> 2) + 1L;
      long chunkSeed = (xSeed * chunkX + zSeed * chunkZ) ^ worldSeed;
      chunkSeed ^= magicNumber;
      return new Random(chunkSeed);
   }
}

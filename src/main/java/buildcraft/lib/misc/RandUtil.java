package buildcraft.lib.misc;

import java.util.Random;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public class RandUtil {
   public static Random createRandomForChunk(Level level, int chunkX, int chunkZ, long magicNumber) {
      long worldSeed = level instanceof ServerLevel sl ? sl.getSeed() : 0L;
      return createRandomForChunk(worldSeed, chunkX, chunkZ, magicNumber);
   }

   public static Random createRandomForChunk(long worldSeed, int chunkX, int chunkZ, long magicNumber) {
      Random worldRandom = new Random(worldSeed);
      long xSeed = worldRandom.nextLong() >> 3;
      long zSeed = worldRandom.nextLong() >> 3;
      long chunkSeed = xSeed * chunkX + zSeed * chunkZ ^ worldSeed;
      chunkSeed ^= magicNumber;
      return new Random(chunkSeed);
   }
}

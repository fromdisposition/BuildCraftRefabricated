package buildcraft.robotics.zone;

public final class ZonePlannerChunkKeys {
   private ZonePlannerChunkKeys() {
   }

   public static long chunkKey(int chunkX, int chunkZ) {
      return (chunkX & 0xFFFFFFFFL) | (long)chunkZ << 32;
   }
}

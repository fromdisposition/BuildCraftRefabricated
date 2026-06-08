package buildcraft.robotics.zone;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Client-side cache of zone planner terrain colours, keyed by {@link net.minecraft.world.level.ChunkPos#asLong}. */
public class ZonePlannerMapColours {
   public static final int NO_HEIGHT = Integer.MIN_VALUE;
   private final Map<Long, int[]> colour = new HashMap<>();
   private final Map<Long, int[]> height = new HashMap<>();
   private final Map<Long, Integer> version = new HashMap<>();
   private final Set<Long> requested = new HashSet<>();
   private int globalVersion;

   public boolean hasData(long key) {
      return this.colour.containsKey(key);
   }

   public boolean isRequested(long key) {
      return this.requested.contains(key);
   }

   public void markRequested(long key) {
      this.requested.add(key);
   }

   public void put(long key, int[] colours, int[] heights) {
      this.colour.put(key, colours);
      this.height.put(key, heights);
      this.version.put(key, ++this.globalVersion);
      this.requested.remove(key);
   }

   /** Monotonic version for a chunk; 0 = no data. Used to invalidate cached render meshes. */
   public int versionOf(long key) {
      return this.version.getOrDefault(key, 0);
   }

   /** Monotonic counter bumped on every {@link #put}; lets renderers detect any terrain change cheaply. */
   public int globalVersion() {
      return this.globalVersion;
   }

   public int colourAt(long key, int localX, int localZ) {
      int[] data = this.colour.get(key);
      return data == null ? 0 : data[(localZ & 15) * 16 + (localX & 15)];
   }

   public int heightAt(long key, int localX, int localZ) {
      int[] data = this.height.get(key);
      return data == null ? NO_HEIGHT : data[(localZ & 15) * 16 + (localX & 15)];
   }

   public void retryMissing() {
      this.requested.clear();
   }

   public void clear() {
      this.colour.clear();
      this.height.clear();
      this.version.clear();
      this.requested.clear();
   }
}

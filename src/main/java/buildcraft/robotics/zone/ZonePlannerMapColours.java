/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.zone;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * Client-side map data of the zone planner, one cache per dimension for the whole play session: a planner opened
 * again shows its surroundings at once from here while the server refreshes them underneath.
 */
public class ZonePlannerMapColours {
   public static final int NO_HEIGHT = Integer.MIN_VALUE;
   private static final int MAX_CHUNKS = 16384;
   private static final Map<ResourceKey<Level>, ZonePlannerMapColours> SESSION = new HashMap<>();
   // Primitive-keyed maps: colourAt/heightAt/versionOf run per cell / per chunk on the render path, and the
   // boxed Long keys of the old HashMaps were a steady per-frame allocation churn.
   private final Long2ObjectOpenHashMap<int[]> colour = new Long2ObjectOpenHashMap<>();
   private final Long2ObjectOpenHashMap<int[]> height = new Long2ObjectOpenHashMap<>();
   private final Long2IntOpenHashMap version = new Long2IntOpenHashMap();
   private int globalVersion;

   public static ZonePlannerMapColours forLevel(Level level) {
      return SESSION.computeIfAbsent(level == null ? null : level.dimension(), key -> new ZonePlannerMapColours());
   }

   public static void clearSession() {
      SESSION.clear();
   }

   public boolean hasData(long key) {
      return this.colour.containsKey(key);
   }

   public void put(long key, int[] colours, int[] heights) {
      if (this.colour.size() >= MAX_CHUNKS && !this.colour.containsKey(key)) {
         this.colour.clear();
         this.height.clear();
         this.version.clear();
      }

      this.colour.put(key, colours);
      this.height.put(key, heights);
      this.version.put(key, ++this.globalVersion);
   }

   public int versionOf(long key) {
      return this.version.get(key);
   }

   public int globalVersion() {
      return this.globalVersion;
   }

   /** Raw backing colour array of a chunk (16x16, row-major by local Z) for mesh baking — treat as read-only. */
   public int[] coloursOf(long key) {
      return this.colour.get(key);
   }

   /** Raw backing height array of a chunk (16x16, row-major by local Z) for mesh baking — treat as read-only. */
   public int[] heightsOf(long key) {
      return this.height.get(key);
   }

   public int colourAt(long key, int localX, int localZ) {
      int[] data = this.colour.get(key);
      return data == null ? 0 : data[(localZ & 15) * 16 + (localX & 15)];
   }

   public int heightAt(long key, int localX, int localZ) {
      int[] data = this.height.get(key);
      return data == null ? NO_HEIGHT : data[(localZ & 15) * 16 + (localX & 15)];
   }
}

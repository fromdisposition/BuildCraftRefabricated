/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.marker;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public abstract class MarkerCache<S extends MarkerSubCache<?>> {
   public static final boolean DEBUG = BCDebugging.shouldDebugLog("lib.markers");
   public static final List<MarkerCache<?>> CACHES = new ArrayList<>();
   public final String name;
   private final Map<ResourceKey<Level>, S> cacheClient = new ConcurrentHashMap<>();
   private final Map<ResourceKey<Level>, S> cacheServer = new ConcurrentHashMap<>();

   public MarkerCache(String name) {
      this.name = name;
   }

   public static void registerCache(MarkerCache<?> cache) {
      CACHES.add(cache);
      if (DEBUG) {
         BCLog.logger.info("[lib.markers] Registered a cache " + cache.name + " with an ID of " + (CACHES.size() - 1));
      }
   }

   public static void postInit() {
      if (DEBUG) {
         BCLog.logger.info("[lib.markers] Sorted list of cache types:");

         for (int i = 0; i < CACHES.size(); i++) {
            MarkerCache<?> cache = CACHES.get(i);
            BCLog.logger.info("  " + i + " = " + cache.name);
         }

         BCLog.logger.info("[lib.markers] Total of " + CACHES.size() + " cache types");
      }
   }

   public static void onPlayerJoinWorld(ServerPlayer player) {
      for (MarkerCache<?> cache : CACHES) {
         Level world = player.level();
         cache.getSubCache(world).onPlayerJoinWorld(player);
      }
   }

   public static void onWorldUnload(Level world) {
      for (MarkerCache<?> cache : CACHES) {
         cache.onWorldUnloadImpl(world);
      }
   }

   /** Client counterpart of {@link #onWorldUnload}: the sub-caches are keyed by dimension id, which collides
    * across worlds/servers in the same game session. The server re-sends all marker state on the next join (and
    * as block entities load), so on disconnect simply drop every client sub-cache — otherwise stale positions
    * from the previous world linger and render as ghost markers/lasers. */
   public static void onClientDisconnect() {
      for (MarkerCache<?> cache : CACHES) {
         cache.cacheClient.clear();
      }
   }

   private void onWorldUnloadImpl(Level world) {
      Map<ResourceKey<Level>, S> cache = world.isClientSide() ? this.cacheClient : this.cacheServer;
      ResourceKey<Level> key = world.dimension();
      cache.remove(key);
   }

   protected abstract S createSubCache(Level var1);

   public S getSubCache(Level world) {
      Map<ResourceKey<Level>, S> cache = world.isClientSide() ? this.cacheClient : this.cacheServer;
      ResourceKey<Level> key = world.dimension();
      return cache.computeIfAbsent(key, k -> this.createSubCache(world));
   }
}

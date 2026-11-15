/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.sync;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import javax.annotation.Nullable;

public final class ClientKeyedCache<K, V> {
   private final Cache<K, V> cache = CacheBuilder.newBuilder()
      //? if >= 26.2 {
      .expireAfterAccess(java.time.Duration.ofMinutes(10))
      //?} else {
      /*.expireAfterAccess(10L, java.util.concurrent.TimeUnit.MINUTES)
      *///?}
      .build();
   private final Set<K> pending = new HashSet<>();
   private final Consumer<K> onMiss;

   public ClientKeyedCache(Consumer<K> onMiss) {
      this.onMiss = onMiss;
   }

   @Nullable
   public V get(K key) {
      V found = this.cache.getIfPresent(key);
      if (found == null && !this.pending.contains(key)) {
         this.pending.add(key);
         this.onMiss.accept(key);
      }

      return found;
   }

   public void put(K key, V value) {
      this.pending.remove(key);
      this.cache.put(key, value);
   }

   public boolean putIfAbsentOrEquals(K key, V value, BiPredicate<V, V> sameContent) {
      this.pending.remove(key);
      V existing = this.cache.getIfPresent(key);
      if (existing != null && sameContent.test(existing, value)) {
         return false;
      }

      this.cache.put(key, value);
      return true;
   }

   public void remove(K key) {
      this.pending.remove(key);
      this.cache.invalidate(key);
   }

   public void request(K key) {
      if (!this.pending.contains(key)) {
         this.pending.add(key);
         this.onMiss.accept(key);
      }
   }
}

/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc.collect;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;

public class TypedKeyMap<K, V> {
   private final Supplier<TypedMap<V>> mapType;
   private final Map<K, TypedMap<V>> internalMap = new HashMap<>();

   public static <K, V> TypedKeyMap<K, V> createDirect() {
      return new TypedKeyMap<>(TypedMapDirect::new);
   }

   public static <K, V> TypedKeyMap<K, V> createHierachy() {
      return new TypedKeyMap<>(TypedMapHierarchy::new);
   }

   private TypedKeyMap(Supplier<TypedMap<V>> mapType) {
      this.mapType = mapType;
   }

   public void put(K key, V value) {
      this.internalMap.computeIfAbsent(key, k -> this.mapType.get()).put(value);
   }

   @Nullable
   public <T extends V> T get(K key, Class<T> clazz) {
      TypedMap<V> m = this.internalMap.get(key);
      return m == null ? null : m.get(clazz);
   }

   public Set<K> getKeys() {
      return this.internalMap.keySet();
   }

   public TypedMap<V> getAll(K key) {
      return this.internalMap.get(key);
   }
}

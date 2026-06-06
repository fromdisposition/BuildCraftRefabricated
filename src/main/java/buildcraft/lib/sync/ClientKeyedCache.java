package buildcraft.lib.sync;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import javax.annotation.Nullable;

public final class ClientKeyedCache<K, V> {
   private final Map<K, V> cache = new HashMap<>();
   private final Set<K> pending = new HashSet<>();
   private final Consumer<K> onMiss;

   public ClientKeyedCache(Consumer<K> onMiss) {
      this.onMiss = onMiss;
   }

   @Nullable
   public V get(K key) {
      V found = this.cache.get(key);
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
      V existing = this.cache.get(key);
      if (existing != null && sameContent.test(existing, value)) {
         return false;
      }

      this.cache.put(key, value);
      return true;
   }

   public void remove(K key) {
      this.pending.remove(key);
      this.cache.remove(key);
   }

   public void request(K key) {
      if (!this.pending.contains(key)) {
         this.pending.add(key);
         this.onMiss.accept(key);
      }
   }

   public boolean isPending(K key) {
      return this.pending.contains(key);
   }
}

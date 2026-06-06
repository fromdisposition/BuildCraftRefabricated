package buildcraft.lib.misc.collect;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

public class TypedMapDirect<V> implements TypedMap<V> {
   private final Map<Class<?>, V> internalMap = new HashMap<>();

   @Nullable
   @Override
   public <T extends V> T get(Class<T> clazz) {
      T val = clazz.cast(this.internalMap.get(clazz));
      return val != null ? val : null;
   }

   @Override
   public void put(V value) {
      this.internalMap.put(value.getClass(), value);
   }

   @Override
   public void clear() {
      this.internalMap.clear();
   }

   @Override
   public void remove(V value) {
      this.internalMap.remove(value.getClass(), value);
   }
}

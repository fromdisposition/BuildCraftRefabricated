package buildcraft.lib.misc.collect;

public interface TypedMap<V> {
   <T extends V> T get(Class<T> var1);

   void put(V var1);

   void clear();

   void remove(V var1);
}

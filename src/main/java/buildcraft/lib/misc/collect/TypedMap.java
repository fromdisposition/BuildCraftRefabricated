package buildcraft.lib.misc.collect;

import net.minecraft.resources.Identifier;

import java.util.Map;

public interface TypedMap<V> {
    <T extends V> T get(Class<T> clazz);

    void put(V value);

    void clear();

    void remove(V value);
}

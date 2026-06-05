package buildcraft.lib.misc.collect;

import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class TypedMapDirect<V> implements TypedMap<V> {

    private final Map<Class<?>, V> internalMap = new HashMap<>();

    @Override
    @Nullable
    public <T extends V> T get(Class<T> clazz) {
        T val = clazz.cast(internalMap.get(clazz));
        if (val != null) {
            return val;
        }
        return null;
    }

    @Override
    public void put(V value) {
        internalMap.put(value.getClass(), value);
    }

    @Override
    public void clear() {
        internalMap.clear();
    }

    @Override
    public void remove(V value) {
        internalMap.remove(value.getClass(), value);
    }
}

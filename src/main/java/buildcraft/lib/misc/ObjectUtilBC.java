package buildcraft.lib.misc;

import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;

public class ObjectUtilBC {

    @Nullable
    public static <T> T castOrNull(Object obj, Class<T> clazz) {
        if (clazz.isInstance(obj)) {
            return clazz.cast(obj);
        } else {
            return null;
        }
    }

    public static <T> T castOrDefault(Object obj, Class<T> clazz, T _default) {
        if (clazz.isInstance(obj)) {
            return clazz.cast(obj);
        } else {
            return _default;
        }
    }
}

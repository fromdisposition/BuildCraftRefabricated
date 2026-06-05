package buildcraft.api.core;

import javax.annotation.Nullable;

public interface IConvertable {

    @Nullable
    default <T> T convertTo(Class<T> clazz) {
        if (clazz.isInstance(this)) {
            return clazz.cast(this);
        }
        return null;
    }
}

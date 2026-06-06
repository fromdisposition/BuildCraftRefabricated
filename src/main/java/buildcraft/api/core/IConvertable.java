package buildcraft.api.core;

import javax.annotation.Nullable;

public interface IConvertable {
   @Nullable
   default <T> T convertTo(Class<T> clazz) {
      return clazz.isInstance(this) ? clazz.cast(this) : null;
   }
}

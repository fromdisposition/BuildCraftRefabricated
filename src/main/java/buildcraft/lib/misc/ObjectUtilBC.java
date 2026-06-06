package buildcraft.lib.misc;

import javax.annotation.Nullable;

public class ObjectUtilBC {
   @Nullable
   public static <T> T castOrNull(Object obj, Class<T> clazz) {
      return clazz.isInstance(obj) ? clazz.cast(obj) : null;
   }

   public static <T> T castOrDefault(Object obj, Class<T> clazz, T _default) {
      return clazz.isInstance(obj) ? clazz.cast(obj) : _default;
   }
}

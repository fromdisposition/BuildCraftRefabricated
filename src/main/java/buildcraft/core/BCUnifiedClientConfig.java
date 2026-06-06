package buildcraft.core;

import java.lang.reflect.Method;

public final class BCUnifiedClientConfig {
   private BCUnifiedClientConfig() {
   }

   public static void onDisplayConfigReloaded() {
      try {
         Class<?> cls = Class.forName("buildcraft.transport.client.model.PipeBaseModelGenStandard");
         Method method = cls.getMethod("onColorBlindToggle");
         method.invoke(null);
      } catch (ClassNotFoundException var2) {
      } catch (ReflectiveOperationException var3) {
      }
   }
}

package buildcraft.lib.client;

import buildcraft.lib.BCLibConfig;
import net.minecraft.client.Minecraft;

public final class ColorBlindUtil {
   private ColorBlindUtil() {
   }

   public static boolean isActive() {
      if (BCLibConfig.colorBlindMode == null) {
         return false;
      }

      BCLibConfig.ColorBlindMode mode = BCLibConfig.colorBlindMode.get();
      switch (mode) {
         case ON:
            return true;
         case OFF:
            return false;
         case AUTO:
         default:
            Minecraft mc = Minecraft.getInstance();
            return mc != null && mc.options != null ? (Boolean)mc.options.highContrast().get() : false;
      }
   }
}

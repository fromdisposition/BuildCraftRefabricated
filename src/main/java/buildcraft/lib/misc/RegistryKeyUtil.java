package buildcraft.lib.misc;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public final class RegistryKeyUtil {
   public static Identifier id(ResourceKey<?> key) {
      return key.identifier();
   }

   private RegistryKeyUtil() {
   }
}

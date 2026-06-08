package buildcraft.core.marker.volume;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.Identifier;

public enum AddonsRegistry {
   INSTANCE;

   private final Map<Identifier, Class<? extends Addon>> addonClasses = new HashMap<>();

   public void register(Identifier name, Class<? extends Addon> clazz) {
      if (!this.addonClasses.containsKey(name)) {
         this.addonClasses.put(name, clazz);
      }
   }

   public Class<? extends Addon> getClassByName(Identifier name) {
      return this.addonClasses.get(name);
   }

   public Identifier getNameByClass(Class<? extends Addon> clazz) {
      return this.addonClasses.entrySet()
         .stream()
         .filter(nameClass -> nameClass.getValue().equals(clazz))
         .map(Map.Entry::getKey)
         .findFirst()
         .orElse(null);
   }
}

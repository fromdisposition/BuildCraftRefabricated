package buildcraft.lib.gui.config;

import buildcraft.api.core.BCLog;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

class GuiConfigSet {
   final Map<String, GuiPropertyBoolean> properties = new TreeMap<>();

   GuiPropertyBoolean getOrAddBoolean(String name, boolean defaultValue) {
      return this.properties.computeIfAbsent(name, n -> new GuiPropertyBoolean(n, defaultValue));
   }

   JsonObject writeToJson() {
      JsonObject json = new JsonObject();

      for (Entry<String, GuiPropertyBoolean> entry : this.properties.entrySet()) {
         json.add(entry.getKey(), entry.getValue().writeToJson());
      }

      return json;
   }

   void readFromJson(JsonObject json) {
      for (Entry<String, JsonElement> entry : json.entrySet()) {
         String name = entry.getKey();
         GuiPropertyBoolean prop = this.properties.get(name);
         if (prop == null) {
            prop = new GuiPropertyBoolean(name, false);
            this.properties.put(name, prop);
         }

         JsonElement elem = entry.getValue();
         if (elem.isJsonPrimitive() && elem.getAsJsonPrimitive().isBoolean()) {
            prop.readFromJson(elem);
         } else {
            BCLog.logger.warn("[lib.gui.config] Non-boolean entry for '" + name + "', skipping");
         }
      }
   }
}

package buildcraft.lib.gui.config;

import buildcraft.api.core.BCLog;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class GuiPropertyBoolean {
   public final String name;
   private boolean value;

   public GuiPropertyBoolean(String name, boolean defaultValue) {
      this.name = name;
      this.value = defaultValue;
   }

   public boolean get() {
      return this.value;
   }

   public void set(boolean value) {
      if (this.value != value) {
         this.value = value;
         GuiConfigManager.markDirty();
      }
   }

   JsonElement writeToJson() {
      return new JsonPrimitive(this.value);
   }

   void readFromJson(JsonElement json) {
      if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isBoolean()) {
         this.value = json.getAsBoolean();
      } else {
         BCLog.logger.warn("[lib.gui.config] Tried to read " + json + " as boolean, but it wasn't!");
      }
   }
}

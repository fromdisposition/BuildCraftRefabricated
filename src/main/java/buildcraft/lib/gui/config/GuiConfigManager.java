package buildcraft.lib.gui.config;

import buildcraft.api.core.BCLog;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

public class GuiConfigManager {
   private static final Map<String, GuiConfigSet> properties = new TreeMap<>();
   private static Path configFile = null;

   public static void init(Path file) {
      configFile = file;
      loadFromConfigFile();
   }

   public static GuiPropertyBoolean getOrAddBoolean(String guiId, String name, boolean defaultValue) {
      GuiConfigSet set = properties.computeIfAbsent(guiId, id -> new GuiConfigSet());
      return set.getOrAddBoolean(name, defaultValue);
   }

   static void markDirty() {
      if (configFile != null) {
         try {
            Files.createDirectories(configFile.getParent());
            JsonObject json = writeToJson();
            String text = new GsonBuilder().setPrettyPrinting().create().toJson(json);
            Files.writeString(configFile, text, StandardCharsets.UTF_8);
         } catch (IOException io) {
            BCLog.logger.warn("[lib.gui.cfg] Failed to write gui state file: " + io.getMessage());
         }
      }
   }

   private static void loadFromConfigFile() {
      if (configFile != null && Files.exists(configFile)) {
         String text;
         try {
            text = Files.readString(configFile, StandardCharsets.UTF_8);
         } catch (IOException io) {
            BCLog.logger.warn("[lib.gui.cfg] Failed to read gui state file: " + io.getMessage());
            return;
         }

         try {
            JsonObject json = (JsonObject)new Gson().fromJson(text, JsonObject.class);
            if (json == null) {
               return;
            }

            readFromJson(json);
         } catch (JsonSyntaxException | ClassCastException ex) {
            BCLog.logger.warn("[lib.gui.cfg] Malformed gui state file (delete to reset): " + ex.getMessage());
         }
      }
   }

   private static JsonObject writeToJson() {
      JsonObject json = new JsonObject();

      for (Entry<String, GuiConfigSet> entry : properties.entrySet()) {
         json.add(entry.getKey(), entry.getValue().writeToJson());
      }

      return json;
   }

   private static void readFromJson(JsonObject json) {
      for (Entry<String, JsonElement> entry : json.entrySet()) {
         String guiId = entry.getKey();
         JsonElement elem = entry.getValue();
         if (!elem.isJsonObject()) {
            BCLog.logger.warn("[lib.gui.cfg] Non-object entry for '" + guiId + "', skipping");
         } else {
            GuiConfigSet set = properties.computeIfAbsent(guiId, k -> new GuiConfigSet());
            set.readFromJson(elem.getAsJsonObject());
         }
      }
   }

   static void resetForTesting() {
      properties.clear();
      configFile = null;
   }
}

package buildcraft.fabric.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import buildcraft.lib.fabric.loader.GamePaths;
import java.util.Map;
import org.slf4j.Logger;

public final class BCObjectsConfig {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static final String FILE_NAME = "buildcraftrefabricated-objects.json";
   private static final Map<String, Boolean> BLOCKS = new HashMap<>();
   private static final Map<String, Boolean> ITEMS = new HashMap<>();
   private static final Map<String, Boolean> PIPES = new HashMap<>();

   private BCObjectsConfig() {
   }

   public static void load() {
      Path path = GamePaths.BUILDCRAFT_CONFIG_DIR.resolve(FILE_NAME);
      BLOCKS.clear();
      ITEMS.clear();
      PIPES.clear();
      if (!Files.exists(path)) {
         write(path, defaults());
         LOGGER.info("Created default objects config at {}", path.toAbsolutePath());
         return;
      }

      try (Reader reader = Files.newBufferedReader(path)) {
         JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
         readCategory(root, "blocks", BLOCKS);
         readCategory(root, "items", ITEMS);
         readCategory(root, "pipes", PIPES);
      } catch (Exception e) {
         LOGGER.error("Failed to read {}, all objects enabled", path, e);
      }
   }

   public static boolean isBlockEnabled(String modid, String path) {
      return isEnabled(BLOCKS, modid + ":" + path);
   }

   public static boolean isItemEnabled(String modid, String path) {
      String id = modid + ":" + path;
      if (PIPES.containsKey(id)) {
         return PIPES.get(id);
      }

      return isEnabled(ITEMS, id);
   }

   public static boolean isPipeEnabled(String modid, String path) {
      return isEnabled(PIPES, modid + ":" + path);
   }

   public static boolean hasBlockBeenDisabled(String modid, String path) {
      return BLOCKS.containsKey(modid + ":" + path) && !BLOCKS.get(modid + ":" + path);
   }

   public static boolean hasItemBeenDisabled(String modid, String path) {
      return ITEMS.containsKey(modid + ":" + path) && !ITEMS.get(modid + ":" + path);
   }

   private static boolean isEnabled(Map<String, Boolean> category, String id) {
      return category.getOrDefault(id, true);
   }

   private static void readCategory(JsonObject root, String key, Map<String, Boolean> target) {
      if (!root.has(key)) {
         return;
      }

      JsonObject section = root.getAsJsonObject(key);
      for (Map.Entry<String, JsonElement> entry : section.entrySet()) {
         target.put(entry.getKey(), entry.getValue().getAsBoolean());
      }
   }

   private static JsonObject defaults() {
      JsonObject root = new JsonObject();
      root.add("blocks", new JsonObject());
      root.add("items", new JsonObject());
      root.add("pipes", new JsonObject());
      return root;
   }

   private static void write(Path path, JsonObject root) {
      try {
         Files.createDirectories(path.getParent());

         try (Writer writer = Files.newBufferedWriter(path)) {
            GSON.toJson(root, writer);
         }
      } catch (IOException e) {
         LOGGER.error("Failed to write {}", path, e);
      }
   }
}

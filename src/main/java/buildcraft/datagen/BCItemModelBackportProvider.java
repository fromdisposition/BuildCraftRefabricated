package buildcraft.datagen;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

/** 1.21.1 resolves item models straight from models/item/&lt;id&gt;.json and has no client item-definition
 * system, so on that node this rebuilds the legacy files from the authored assets/&lt;ns&gt;/items definitions
 * (read off the datagen classpath): a plain model definition becomes a parent stub, and a custom_model_data
 * range_dispatch becomes integer overrides merged onto the base model. Later nodes emit nothing here. */
public final class BCItemModelBackportProvider implements DataProvider {
   //? if < 1.21.10 {
   /*private final PackOutput.PathProvider pathProvider;
   *///?}

   public BCItemModelBackportProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
      //? if < 1.21.10 {
      /*this.pathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models/item");
      *///?}
   }

   @Override
   public CompletableFuture<?> run(CachedOutput cache) {
      //? if < 1.21.10 {
      /*java.util.List<CompletableFuture<?>> futures = new java.util.ArrayList<>();
      for (net.minecraft.world.item.Item item : net.minecraft.core.registries.BuiltInRegistries.ITEM) {
         Identifier id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item);
         if (!id.getNamespace().startsWith("buildcraft")) {
            continue;
         }

         JsonObject definition = readClasspathJson("/assets/" + id.getNamespace() + "/items/" + id.getPath() + ".json");
         if (definition == null) {
            continue;
         }

         JsonObject existing = readClasspathJson("/assets/" + id.getNamespace() + "/models/item/" + id.getPath() + ".json");
         JsonObject converted = convert(definition, existing);
         if (converted != null) {
            futures.add(DataProvider.saveStable(cache, converted, this.pathProvider.json(id)));
         }
      }

      return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
      *///?} else {
      return CompletableFuture.completedFuture(null);
      //?}
   }

   //? if < 1.21.10 {
   /*private static JsonObject convert(JsonObject definition, JsonObject existing) {
      JsonObject model = definition.getAsJsonObject("model");
      if (model == null) {
         return null;
      }

      com.google.gson.JsonArray overrides = new com.google.gson.JsonArray();
      String baseRef = null;
      String type = model.has("type") ? model.get("type").getAsString() : null;
      if ("minecraft:range_dispatch".equals(type)) {
         if (!"minecraft:custom_model_data".equals(model.get("property").getAsString())) {
            return null;
         }

         for (JsonElement entryElement : model.getAsJsonArray("entries")) {
            JsonObject entry = entryElement.getAsJsonObject();
            JsonObject predicate = new JsonObject();
            predicate.addProperty("custom_model_data", entry.get("threshold").getAsInt());
            JsonObject override = new JsonObject();
            override.add("predicate", predicate);
            override.addProperty("model", entry.getAsJsonObject("model").get("model").getAsString());
            overrides.add(override);
         }

         JsonObject fallback = model.getAsJsonObject("fallback");
         if (fallback != null) {
            baseRef = fallback.get("model").getAsString();
         }
      } else if ("minecraft:model".equals(type)) {
         baseRef = model.get("model").getAsString();
      } else {
         return null;
      }

      if (existing != null) {
         if (overrides.isEmpty()) {
            return null;
         }

         existing.add("overrides", overrides);
         return existing;
      }

      if (baseRef == null) {
         return null;
      }

      JsonObject out = new JsonObject();
      out.addProperty("parent", baseRef);
      if (!overrides.isEmpty()) {
         out.add("overrides", overrides);
      }

      return out;
   }

   private static JsonObject readClasspathJson(String path) {
      try (java.io.InputStream in = BCItemModelBackportProvider.class.getResourceAsStream(path)) {
         if (in == null) {
            return null;
         }

         return JsonParser.parseString(new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
      } catch (java.io.IOException e) {
         throw new java.io.UncheckedIOException(path, e);
      }
   }
   *///?}

   @Override
   public String getName() {
      return "BuildCraft Legacy Item Models";
   }
}

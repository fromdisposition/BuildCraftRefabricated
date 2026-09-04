package buildcraft.datagen;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

/** 1.21.1 has no item-definition system and resolves models straight from models/item/&lt;id&gt;.json, so this rebuilds those legacy files from {@link BCAssetDefs}; later nodes emit nothing here. */
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
      BCAssetDefs.ITEM_MODELS.forEach((id, ref) -> emit(cache, futures, id, ref, null));
      for (BCAssetDefs.Dispatch dispatch : BCAssetDefs.ITEM_DISPATCH) {
         emit(cache, futures, dispatch.id(), dispatch.fallback(), dispatch.entries());
      }

      return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
      *///?} else {
      return CompletableFuture.completedFuture(null);
      //?}
   }

   //? if < 1.21.10 {
   /*private void emit(
      CachedOutput cache, java.util.List<CompletableFuture<?>> futures, String id, String baseRef, java.util.Map<Integer, String> entries
   ) {
      Identifier itemId = Identifier.parse(id);
      JsonObject existing = readClasspathJson("/assets/" + itemId.getNamespace() + "/models/item/" + itemId.getPath() + ".json");
      com.google.gson.JsonArray overrides = new com.google.gson.JsonArray();
      if (entries != null) {
         entries.forEach((threshold, ref) -> {
            JsonObject predicate = new JsonObject();
            predicate.addProperty("custom_model_data", threshold);
            JsonObject override = new JsonObject();
            override.add("predicate", predicate);
            override.addProperty("model", ref);
            overrides.add(override);
         });
      }

      JsonObject out;
      if (existing != null) {
         if (overrides.isEmpty()) {
            return;
         }

         existing.add("overrides", overrides);
         out = existing;
      } else {
         out = new JsonObject();
         out.addProperty("parent", baseRef);
         if (!overrides.isEmpty()) {
            out.add("overrides", overrides);
         }
      }

      futures.add(DataProvider.saveStable(cache, out, this.pathProvider.json(itemId)));
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

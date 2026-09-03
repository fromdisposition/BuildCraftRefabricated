package buildcraft.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

/** Emits the boilerplate client assets from {@link BCAssetDefs}: item model definitions (1.21.10+ only —
 * 1.21.1 has no item-definition system and gets its legacy models from
 * {@link BCItemModelBackportProvider}), flat item models, single-variant and four-way-facing blockstates,
 * and the trivial cube block models. */
public final class BCAssetProvider implements DataProvider {
   private final PackOutput.PathProvider items;
   private final PackOutput.PathProvider itemModels;
   private final PackOutput.PathProvider blockstates;
   private final PackOutput.PathProvider blockModels;

   public BCAssetProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
      this.items = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "items");
      this.itemModels = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models/item");
      this.blockstates = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "blockstates");
      this.blockModels = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models/block");
   }

   @Override
   public CompletableFuture<?> run(CachedOutput cache) {
      List<CompletableFuture<?>> futures = new ArrayList<>();

      //? if >= 1.21.10 {
      BCAssetDefs.ITEM_MODELS.forEach((id, ref) ->
         futures.add(DataProvider.saveStable(cache, itemDefinition(model(ref)), this.items.json(Identifier.parse(id)))));
      for (BCAssetDefs.Dispatch dispatch : BCAssetDefs.ITEM_DISPATCH) {
         JsonObject root = new JsonObject();
         root.addProperty("type", "minecraft:range_dispatch");
         root.addProperty("property", "minecraft:custom_model_data");
         JsonArray entries = new JsonArray();
         dispatch.entries().forEach((threshold, ref) -> {
            JsonObject entry = new JsonObject();
            entry.addProperty("threshold", threshold);
            entry.add("model", model(ref));
            entries.add(entry);
         });
         root.add("entries", entries);
         root.add("fallback", model(dispatch.fallback()));
         futures.add(DataProvider.saveStable(cache, itemDefinition(root), this.items.json(Identifier.parse(dispatch.id()))));
      }
      //?}

      for (BCAssetDefs.FlatModel flat : BCAssetDefs.FLAT_ITEM_MODELS) {
         JsonObject textures = new JsonObject();
         textures.addProperty("layer0", flat.texture());
         JsonObject root = new JsonObject();
         root.addProperty("parent", flat.parent());
         root.add("textures", textures);
         futures.add(DataProvider.saveStable(cache, root, this.itemModels.json(Identifier.parse(flat.id()))));
      }

      BCAssetDefs.BLOCKSTATES_SINGLE.forEach((id, modelRef) -> {
         JsonObject variant = new JsonObject();
         variant.addProperty("model", modelRef);
         JsonObject variants = new JsonObject();
         variants.add("", variant);
         JsonObject root = new JsonObject();
         root.add("variants", variants);
         futures.add(DataProvider.saveStable(cache, root, this.blockstates.json(Identifier.parse(id))));
      });

      BCAssetDefs.BLOCKSTATES_FACING.forEach((id, modelRef) -> {
         JsonObject variants = new JsonObject();
         variants.add("facing=north", facingVariant(modelRef, 0));
         variants.add("facing=east", facingVariant(modelRef, 90));
         variants.add("facing=south", facingVariant(modelRef, 180));
         variants.add("facing=west", facingVariant(modelRef, 270));
         JsonObject root = new JsonObject();
         root.add("variants", variants);
         futures.add(DataProvider.saveStable(cache, root, this.blockstates.json(Identifier.parse(id))));
      });

      for (BCAssetDefs.Model model : BCAssetDefs.PIPE_ITEM_MODELS) {
         futures.add(DataProvider.saveStable(cache, model(model.parent(), model.textures()), this.itemModels.json(Identifier.parse(model.id()))));
      }

      BCAssetDefs.PARENT_ITEM_MODELS.forEach((id, parent) ->
         futures.add(DataProvider.saveStable(cache, model(parent, Map.of()), this.itemModels.json(Identifier.parse(id)))));
      BCAssetDefs.PARTICLE_BLOCK_MODELS.forEach((id, particle) ->
         futures.add(DataProvider.saveStable(cache, model(null, Map.of("particle", particle)), this.blockModels.json(Identifier.parse(id)))));

      for (String fluid : BCAssetDefs.FLUIDS) {
         Identifier block = Identifier.fromNamespaceAndPath("buildcraftenergy", fluid);
         futures.add(DataProvider.saveStable(
            cache,
            model("minecraft:item/generated", Map.of("layer0", "minecraft:item/bucket", "layer1", "buildcraftenergy:item/bucket_fluid/" + fluid)),
            this.itemModels.json(Identifier.fromNamespaceAndPath("buildcraftenergy", "fluid_buckets/" + fluid + "_bucket"))
         ));
         futures.add(DataProvider.saveStable(cache, model(null, Map.of("particle", "buildcraftenergy:block/fluids/baked/" + fluid)), this.blockModels.json(block)));
         JsonObject apply = new JsonObject();
         apply.addProperty("model", "buildcraftenergy:block/" + fluid);
         apply.addProperty("x", 0);
         apply.addProperty("y", 0);
         JsonObject part = new JsonObject();
         part.add("apply", apply);
         JsonArray multipart = new JsonArray();
         multipart.add(part);
         JsonObject root = new JsonObject();
         root.add("multipart", multipart);
         futures.add(DataProvider.saveStable(cache, root, this.blockstates.json(block)));
      }

      for (BCAssetDefs.BlockModel blockModel : BCAssetDefs.BLOCK_MODELS) {
         JsonObject textures = new JsonObject();
         for (Map.Entry<String, String> texture : blockModel.textures().entrySet()) {
            textures.addProperty(texture.getKey(), texture.getValue());
         }
         JsonObject root = new JsonObject();
         root.addProperty("parent", blockModel.parent());
         root.add("textures", textures);
         futures.add(DataProvider.saveStable(cache, root, this.blockModels.json(Identifier.parse(blockModel.id()))));
      }

      return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
   }

   private static JsonObject model(String parent, Map<String, String> textures) {
      JsonObject root = new JsonObject();
      if (parent != null) {
         root.addProperty("parent", parent);
      }

      if (!textures.isEmpty()) {
         JsonObject json = new JsonObject();
         textures.forEach(json::addProperty);
         root.add("textures", json);
      }

      return root;
   }

   private static JsonObject model(String ref) {
      JsonObject model = new JsonObject();
      model.addProperty("type", "minecraft:model");
      model.addProperty("model", ref);
      return model;
   }

   private static JsonObject itemDefinition(JsonObject model) {
      JsonObject root = new JsonObject();
      root.add("model", model);
      return root;
   }

   private static JsonObject facingVariant(String modelRef, int rotation) {
      JsonObject variant = new JsonObject();
      variant.addProperty("model", modelRef);
      if (rotation != 0) {
         variant.addProperty("y", rotation);
      }
      return variant;
   }

   @Override
   public String getName() {
      return "BuildCraft Assets";
   }
}

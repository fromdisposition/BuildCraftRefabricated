package buildcraft.datagen;

import buildcraft.lib.fabric.BcRegistryUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
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
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;

/** Carries a Fabric load condition so a game without the companion mod never parses this table. */
public final class BCConditionalLootProvider implements DataProvider {
   private static final Map<String, String> MOD_GATED = Map.of(
      "buildcraftenergy:engine_rf", "team_reborn_energy",
      "buildcraftenergy:mj_dynamo", "team_reborn_energy"
   );
   private final PackOutput.PathProvider pathProvider;
   private final CompletableFuture<HolderLookup.Provider> registries;

   public BCConditionalLootProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
      this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "loot_table");
      this.registries = registries;
   }

   @Override
   public CompletableFuture<?> run(CachedOutput cache) {
      return this.registries.thenCompose(provider -> {
         RegistryOps<JsonElement> ops = provider.createSerializationContext(JsonOps.INSTANCE);
         List<CompletableFuture<?>> futures = new ArrayList<>();
         for (Map.Entry<String, String> entry : MOD_GATED.entrySet()) {
            Identifier id = Identifier.parse(entry.getKey());
            Block block = BcRegistryUtil.getBlock(id);
            if (block == null) {
               continue;
            }
            LootTable table = LootTable.lootTable()
               .withPool(LootPool.lootPool().add(LootItem.lootTableItem(block)).when(ExplosionCondition.survivesExplosion()))
               .setParamSet(LootContextParamSets.BLOCK)
               .build();
            JsonObject json = LootTable.DIRECT_CODEC.encodeStart(ops, table).getOrThrow().getAsJsonObject();
            JsonObject condition = new JsonObject();
            condition.addProperty("condition", "fabric:all_mods_loaded");
            JsonArray values = new JsonArray();
            values.add(entry.getValue());
            condition.add("values", values);
            JsonArray conditions = new JsonArray();
            conditions.add(condition);
            json.add("fabric:load_conditions", conditions);
            futures.add(DataProvider.saveStable(cache, json, this.pathProvider.json(Identifier.fromNamespaceAndPath(id.getNamespace(), "blocks/" + id.getPath()))));
         }
         return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
      });
   }

   @Override
   public String getName() {
      return "BuildCraft Refabricated/Conditional block loot";
   }
}

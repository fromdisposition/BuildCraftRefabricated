package buildcraft.energy.worldgen.datagen;

import buildcraft.energy.worldgen.core.BCEnergyBiomeTags;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

final class BCEnergyBiomeTagProvider extends FabricTagsProvider<Biome> {
   private static final List<ResourceKey<Biome>> FIELD_DESERT = List.of(Biomes.DESERT, Biomes.BADLANDS, Biomes.WOODED_BADLANDS);
   private static final List<ResourceKey<Biome>> FIELD_OCEAN = List.of(
      Biomes.OCEAN,
      Biomes.DEEP_OCEAN,
      Biomes.COLD_OCEAN,
      Biomes.DEEP_COLD_OCEAN,
      Biomes.LUKEWARM_OCEAN,
      Biomes.DEEP_LUKEWARM_OCEAN,
      Biomes.WARM_OCEAN
   );
   private static final Set<ResourceKey<Biome>> NON_OVERWORLD = Set.of(
      Biomes.THE_VOID,
      Biomes.THE_END,
      Biomes.SMALL_END_ISLANDS,
      Biomes.END_MIDLANDS,
      Biomes.END_HIGHLANDS,
      Biomes.END_BARRENS,
      Biomes.NETHER_WASTES,
      Biomes.CRIMSON_FOREST,
      Biomes.WARPED_FOREST,
      Biomes.SOUL_SAND_VALLEY,
      Biomes.BASALT_DELTAS
   );
   private static final Set<ResourceKey<Biome>> EXCLUDED = Set.of(
      Biomes.THE_VOID, Biomes.RIVER, Biomes.FROZEN_OCEAN, Biomes.DEEP_FROZEN_OCEAN
   );

   BCEnergyBiomeTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
      super(output, Registries.BIOME, registriesFuture);
   }

   @Override
   protected void addTags(HolderLookup.Provider provider) {
      // Sorted by id: Set.of iteration order varies per JVM run and would churn the generated JSON.
      var excluded = builder(BCEnergyBiomeTags.OIL_EXCLUDED_BIOME);
      EXCLUDED.stream()
         .sorted(Comparator.comparing(key -> buildcraft.lib.misc.RegistryKeyUtil.id(key).toString()))
         .forEach(excluded::add);

      var ocean = builder(BCEnergyBiomeTags.OIL_OCEAN);
      for (ResourceKey<Biome> key : FIELD_OCEAN) {
         ocean.add(key);
      }

      var desert = builder(BCEnergyBiomeTags.OIL_DESERT);
      for (ResourceKey<Biome> key : FIELD_DESERT) {
         desert.add(key);
      }

      List<ResourceKey<Biome>> normalSpawnBiomes = new ArrayList<>();
      HolderLookup.RegistryLookup<Biome> biomes = provider.lookupOrThrow(Registries.BIOME);
      for (Holder.Reference<Biome> biome : biomes.listElements().toList()) {
         ResourceKey<Biome> key = biome.key();
         if (NON_OVERWORLD.contains(key) || EXCLUDED.contains(key)) {
            continue;
         }
         normalSpawnBiomes.add(key);
      }
      normalSpawnBiomes.sort(Comparator.comparing(key -> buildcraft.lib.misc.RegistryKeyUtil.id(key).toString()));

      var normalSpawn = builder(BCEnergyBiomeTags.OIL_SPAWN_NORMAL);
      for (ResourceKey<Biome> key : normalSpawnBiomes) {
         normalSpawn.add(key);
      }
   }
}

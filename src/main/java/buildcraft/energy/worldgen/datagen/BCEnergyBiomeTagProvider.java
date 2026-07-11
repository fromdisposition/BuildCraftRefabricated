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
   // Biome ids that exist on the generator node (26.x) but not on every target: emitted as optional tag
   // entries ({"id":...,"required":false}) so the shared tag still loads where the biome is absent.
   // minecraft:pale_garden was added in 1.21.4, minecraft:sulfur_caves in 26.2 — a required entry for a
   // biome the target lacks makes MC drop the WHOLE tag, so normal oil stops spawning there.
   // Compared by id string (RegistryKeyUtil) to stay independent of each node's mappings.
   private static final Set<String> OPTIONAL_NORMAL_SPAWN = Set.of("minecraft:pale_garden", "minecraft:sulfur_caves");
   private static final List<ResourceKey<Biome>> PATCH_DESERT = List.of(Biomes.DESERT, Biomes.BADLANDS, Biomes.WOODED_BADLANDS);
   private static final List<ResourceKey<Biome>> PATCH_OCEAN = List.of(
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
      var excluded = builder(BCEnergyBiomeTags.OIL_EXCLUDED_BIOME);
      for (ResourceKey<Biome> key : EXCLUDED) {
         excluded.add(key);
      }

      var ocean = builder(BCEnergyBiomeTags.OIL_OCEAN);
      for (ResourceKey<Biome> key : PATCH_OCEAN) {
         ocean.add(key);
      }

      var desert = builder(BCEnergyBiomeTags.OIL_DESERT);
      for (ResourceKey<Biome> key : PATCH_DESERT) {
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
         if (OPTIONAL_NORMAL_SPAWN.contains(buildcraft.lib.misc.RegistryKeyUtil.id(key).toString())) {
            normalSpawn.addOptional(key);
         } else {
            normalSpawn.add(key);
         }
      }
   }
}

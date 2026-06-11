package buildcraft.energy.worldgen.datagen;

import buildcraft.energy.worldgen.core.BCEnergyBiomeTags;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

final class BCEnergyBiomeTagProvider extends FabricTagsProvider<Biome> {
   private static final List<ResourceKey<Biome>> PATCH_DESERT = List.of(Biomes.DESERT, Biomes.BADLANDS, Biomes.WOODED_BADLANDS);
   private static final List<ResourceKey<Biome>> PATCH_OCEAN = List.of(
      Biomes.OCEAN,
      Biomes.DEEP_OCEAN,
      Biomes.COLD_OCEAN,
      Biomes.DEEP_COLD_OCEAN,
      Biomes.FROZEN_OCEAN,
      Biomes.DEEP_FROZEN_OCEAN,
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
   private static final Set<ResourceKey<Biome>> EXCLUDED = Set.of(Biomes.THE_VOID, Biomes.RIVER);

   BCEnergyBiomeTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
      super(output, Registries.BIOME, registriesFuture);
   }

   @Override
   protected void addTags(HolderLookup.Provider provider) {
      builder(BCEnergyBiomeTags.OIL_EXCLUDED_BIOME).add(Biomes.THE_VOID, Biomes.RIVER);
      builder(BCEnergyBiomeTags.OIL_DESIGN_BIOME)
         .add(PATCH_DESERT.toArray(ResourceKey[]::new))
         .addOptional(ResourceKey.create(Registries.BIOME, Identifier.parse("buildcraftenergy:oil_ocean")))
         .addOptional(ResourceKey.create(Registries.BIOME, Identifier.parse("buildcraftenergy:oil_desert")));
      builder(BCEnergyBiomeTags.OIL_PATCH_OCEAN).add(PATCH_OCEAN.toArray(ResourceKey[]::new));
      builder(BCEnergyBiomeTags.OIL_PATCH_DESERT).add(PATCH_DESERT.toArray(ResourceKey[]::new));

      Set<ResourceKey<Biome>> patchKeys = new HashSet<>();
      patchKeys.addAll(PATCH_DESERT);
      patchKeys.addAll(PATCH_OCEAN);

      var normalSpawn = builder(BCEnergyBiomeTags.OIL_SPAWN_NORMAL);
      HolderLookup.RegistryLookup<Biome> biomes = provider.lookupOrThrow(Registries.BIOME);
      for (Holder.Reference<Biome> biome : biomes.listElements().toList()) {
         ResourceKey<Biome> key = biome.key();
         if (NON_OVERWORLD.contains(key) || EXCLUDED.contains(key) || patchKeys.contains(key)) {
            continue;
         }
         normalSpawn.add(key);
      }
   }
}

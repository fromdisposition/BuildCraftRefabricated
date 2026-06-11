package buildcraft.energy.generation.datagen;

import buildcraft.energy.generation.core.BCEnergyBiomeTags;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biomes;

final class BCEnergyBiomeTagProvider extends FabricTagsProvider<net.minecraft.world.level.biome.Biome> {
   BCEnergyBiomeTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
      super(output, Registries.BIOME, registriesFuture);
   }

   @Override
   protected void addTags(HolderLookup.Provider provider) {
      builder(BCEnergyBiomeTags.OIL_RICH_BIOME).add(Biomes.DESERT, Biomes.BADLANDS, Biomes.WOODED_BADLANDS);
      builder(BCEnergyBiomeTags.OIL_EXCLUDED_BIOME).add(Biomes.THE_VOID, Biomes.RIVER);
      builder(BCEnergyBiomeTags.OIL_DESIGN_BIOME)
         .add(Biomes.DESERT, Biomes.BADLANDS, Biomes.WOODED_BADLANDS)
         .addOptional(ResourceKey.create(Registries.BIOME, Identifier.parse("buildcraftenergy:oil_ocean")))
         .addOptional(ResourceKey.create(Registries.BIOME, Identifier.parse("buildcraftenergy:oil_desert")));
      builder(BCEnergyBiomeTags.OIL_PATCH_OCEAN).addTag(BiomeTags.IS_OCEAN);
      builder(BCEnergyBiomeTags.OIL_PATCH_DESERT).add(Biomes.DESERT, Biomes.BADLANDS, Biomes.WOODED_BADLANDS);
   }
}

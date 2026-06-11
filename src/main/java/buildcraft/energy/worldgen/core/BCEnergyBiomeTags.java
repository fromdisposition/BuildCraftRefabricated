package buildcraft.energy.worldgen.core;

import buildcraft.fabric.BCRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public final class BCEnergyBiomeTags {
   public static final TagKey<Biome> OIL_RICH_BIOME = TagKey.create(
      Registries.BIOME, BCRegistries.id("buildcraftenergy", "oil_rich_biome")
   );
   public static final TagKey<Biome> OIL_EXCLUDED_BIOME = TagKey.create(
      Registries.BIOME, BCRegistries.id("buildcraftenergy", "oil_excluded_biome")
   );
   public static final TagKey<Biome> OIL_DESIGN_BIOME = TagKey.create(
      Registries.BIOME, BCRegistries.id("buildcraftenergy", "oil_design_biome")
   );
   public static final TagKey<Biome> OIL_PATCH_OCEAN = TagKey.create(
      Registries.BIOME, BCRegistries.id("buildcraftenergy", "oil_patch_ocean")
   );
   public static final TagKey<Biome> OIL_PATCH_DESERT = TagKey.create(
      Registries.BIOME, BCRegistries.id("buildcraftenergy", "oil_patch_desert")
   );

   private BCEnergyBiomeTags() {
   }
}

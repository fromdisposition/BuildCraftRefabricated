package buildcraft.energy.worldgen.core;

import buildcraft.fabric.BCRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public final class BCEnergyBiomeTags {
   public static final TagKey<Biome> OIL_EXCLUDED_BIOME = TagKey.create(
      Registries.BIOME, BCRegistries.id("buildcraftenergy", "oil_excluded_biome")
   );
   /** Vanilla ocean biomes eligible for per-chunk patch-tier slicing ({@code oilOceanPatchChancePercent}). */
   public static final TagKey<Biome> OIL_PATCH_OCEAN = TagKey.create(
      Registries.BIOME, BCRegistries.id("buildcraftenergy", "oil_patch_ocean")
   );
   /** Desert/badlands eligible for per-chunk rich-tier slicing ({@code oilDesertRichChancePercent}). */
   public static final TagKey<Biome> OIL_PATCH_DESERT = TagKey.create(
      Registries.BIOME, BCRegistries.id("buildcraftenergy", "oil_patch_desert")
   );
   /** Overworld minus excluded and oceans; desert stays for normal-tier slices. */
   public static final TagKey<Biome> OIL_SPAWN_NORMAL = TagKey.create(
      Registries.BIOME, BCRegistries.id("buildcraftenergy", "oil_spawn_normal")
   );

   private BCEnergyBiomeTags() {
   }
}

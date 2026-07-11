package buildcraft.energy.worldgen.core;

import buildcraft.fabric.BCRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public final class BCEnergyBiomeTags {
   public static final TagKey<Biome> OIL_EXCLUDED_BIOME = TagKey.create(
      Registries.BIOME, BCRegistries.id("buildcraftenergy", "oil_excluded_biome")
   );
   /**
    * Vanilla ocean biomes where oil tier routing applies. Tag membership ≠ rich sector;
    * rich patch sectors are chosen by {@link buildcraft.energy.worldgen.structure.OilStructureSpawnConditions}
    * sector roll ({@code oilOceanPatchChancePercent}).
    */
   public static final TagKey<Biome> OIL_OCEAN = TagKey.create(
      Registries.BIOME, BCRegistries.id("buildcraftenergy", "has_structure/oil_field_ocean")
   );
   /**
    * Desert/badlands where oil tier routing applies. Tag membership ≠ rich sector;
    * rich sectors use {@code oilDesertRichChancePercent} on 8×8 chunk sectors.
    */
   public static final TagKey<Biome> OIL_DESERT = TagKey.create(
      Registries.BIOME, BCRegistries.id("buildcraftenergy", "has_structure/oil_field_desert")
   );
   /** Overworld minus excluded biomes; base {@code oil_well} tier (includes ocean and desert). */
   public static final TagKey<Biome> OIL_SPAWN_NORMAL = TagKey.create(
      Registries.BIOME, BCRegistries.id("buildcraftenergy", "has_structure/oil_well")
   );

   private BCEnergyBiomeTags() {
   }
}

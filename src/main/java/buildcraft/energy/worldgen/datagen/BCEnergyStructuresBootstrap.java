package buildcraft.energy.worldgen.datagen;

import buildcraft.energy.worldgen.core.BCEnergyBiomeTags;
import buildcraft.energy.worldgen.structure.BCEnergyStructures;
import buildcraft.energy.worldgen.structure.OilDepositStructure;
import buildcraft.energy.worldgen.structure.OilStructureSpawnConditions;
import buildcraft.energy.worldgen.structure.WaterSpringStructure;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.heightproviders.ConstantHeight;
import net.minecraft.world.level.levelgen.VerticalAnchor;

final class BCEnergyStructuresBootstrap {
   private BCEnergyStructuresBootstrap() {
   }

   static void bootstrap(BootstrapContext<Structure> context) {
      HolderGetter<StructureTemplatePool> pools = context.lookup(Registries.TEMPLATE_POOL);
      HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);

      registerTier(
         context,
         biomes,
         BCEnergyStructures.OIL_WELL,
         pools.getOrThrow(BCEnergyTemplatePoolsBootstrap.NORMAL_START),
         BCEnergyBiomeTags.OIL_SPAWN_NORMAL,
         OilStructureSpawnConditions.Tier.NORMAL
      );
      registerField(
         context,
         biomes,
         BCEnergyStructures.OIL_FIELD_DESERT,
         pools.getOrThrow(BCEnergyTemplatePoolsBootstrap.FIELD_DESERT_START),
         BCEnergyBiomeTags.OIL_DESERT,
         OilStructureSpawnConditions.Tier.FIELD_DESERT
      );
      registerField(
         context,
         biomes,
         BCEnergyStructures.OIL_FIELD_OCEAN,
         pools.getOrThrow(BCEnergyTemplatePoolsBootstrap.FIELD_OCEAN_START),
         BCEnergyBiomeTags.OIL_OCEAN,
         OilStructureSpawnConditions.Tier.FIELD_OCEAN
      );

      context.register(
         BCEnergyStructures.WATER_SPRING,
         new WaterSpringStructure(
            new Structure.StructureSettings.Builder(biomes.getOrThrow(BiomeTags.IS_OVERWORLD))
               .generationStep(net.minecraft.world.level.levelgen.GenerationStep.Decoration.UNDERGROUND_DECORATION)
               .terrainAdapation(TerrainAdjustment.NONE)
               .build(),
            pools.getOrThrow(BCEnergyTemplatePoolsBootstrap.WATER_SPRING_START),
            java.util.Optional.empty(),
            1,
            ConstantHeight.of(VerticalAnchor.absolute(0)),
            false,
            java.util.Optional.empty(),
            //? if >= 1.21.10 {
            new JigsawStructure.MaxDistance(1),
            //?} else {
            /*1,
            *///?}
            java.util.List.of(),
            JigsawStructure.DEFAULT_DIMENSION_PADDING,
            JigsawStructure.DEFAULT_LIQUID_SETTINGS
         )
      );
   }

   /** A field is ONE structure clustering several wells/lakes (mineshaft-style pieces) around its centre. */
   private static void registerField(
      BootstrapContext<Structure> context,
      HolderGetter<Biome> biomes,
      net.minecraft.resources.ResourceKey<Structure> key,
      Holder<StructureTemplatePool> pool,
      TagKey<Biome> biomeTag,
      OilStructureSpawnConditions.Tier tier
   ) {
      context.register(
         key,
         new buildcraft.energy.worldgen.structure.OilFieldStructure(
            new Structure.StructureSettings.Builder(biomes.getOrThrow(biomeTag))
               .generationStep(net.minecraft.world.level.levelgen.GenerationStep.Decoration.UNDERGROUND_STRUCTURES)
               .terrainAdapation(TerrainAdjustment.NONE)
               .build(),
            pool,
            net.minecraft.util.valueproviders.UniformInt.of(6, 9),
            80,
            tier
         )
      );
   }

   private static void registerTier(
      BootstrapContext<Structure> context,
      HolderGetter<Biome> biomes,
      net.minecraft.resources.ResourceKey<Structure> key,
      Holder<StructureTemplatePool> startPool,
      TagKey<Biome> biomeTag,
      OilStructureSpawnConditions.Tier tier
   ) {
      context.register(
         key,
         new OilDepositStructure(
            new Structure.StructureSettings.Builder(biomes.getOrThrow(biomeTag))
               .generationStep(net.minecraft.world.level.levelgen.GenerationStep.Decoration.UNDERGROUND_STRUCTURES)
               .terrainAdapation(TerrainAdjustment.NONE)
               .build(),
            startPool,
            // Centre anchor: random rotation spins the template around the chunk middle, so the oil column
            // always sits exactly where /locate (chunk corner + locate_offset 8,8) points.
            java.util.Optional.of(buildcraft.fabric.BCRegistries.id("buildcraftenergy", "well_anchor")),
            1,
            ConstantHeight.of(VerticalAnchor.absolute(0)),
            false,
            java.util.Optional.of(Heightmap.Types.WORLD_SURFACE_WG),
            //? if >= 1.21.10 {
            new JigsawStructure.MaxDistance(1),
            //?} else {
            /*1,
            *///?}
            java.util.List.of(),
            JigsawStructure.DEFAULT_DIMENSION_PADDING,
            JigsawStructure.DEFAULT_LIQUID_SETTINGS,
            tier
         )
      );
   }
}

package buildcraft.energy.worldgen.datagen;

import buildcraft.energy.worldgen.core.BCEnergyBiomeTags;
import buildcraft.energy.worldgen.structure.BCEnergyStructures;
import buildcraft.energy.worldgen.structure.OilDepositStructure;
import buildcraft.energy.worldgen.structure.OilStructureSpawnConditions;
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
         BCEnergyStructures.OIL_DEPOSIT_NORMAL,
         pools.getOrThrow(BCEnergyTemplatePoolsBootstrap.NORMAL_START),
         BiomeTags.IS_OVERWORLD,
         OilStructureSpawnConditions.Tier.NORMAL
      );
      registerTier(
         context,
         biomes,
         BCEnergyStructures.OIL_DEPOSIT_RICH,
         pools.getOrThrow(BCEnergyTemplatePoolsBootstrap.RICH_START),
         BCEnergyBiomeTags.OIL_RICH_BIOME,
         OilStructureSpawnConditions.Tier.RICH
      );
      registerTier(
         context,
         biomes,
         BCEnergyStructures.OIL_DEPOSIT_PATCH_DESERT,
         pools.getOrThrow(BCEnergyTemplatePoolsBootstrap.PATCH_START),
         BCEnergyBiomeTags.OIL_PATCH_DESERT,
         OilStructureSpawnConditions.Tier.PATCH_DESERT
      );
      registerTier(
         context,
         biomes,
         BCEnergyStructures.OIL_DEPOSIT_PATCH_OCEAN,
         pools.getOrThrow(BCEnergyTemplatePoolsBootstrap.PATCH_START),
         BCEnergyBiomeTags.OIL_PATCH_OCEAN,
         OilStructureSpawnConditions.Tier.PATCH_OCEAN
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
               .terrainAdapation(TerrainAdjustment.BEARD_THIN)
               .build(),
            startPool,
            java.util.Optional.empty(),
            1,
            ConstantHeight.of(VerticalAnchor.absolute(0)),
            true,
            java.util.Optional.of(Heightmap.Types.WORLD_SURFACE_WG),
            new JigsawStructure.MaxDistance(80),
            java.util.List.of(),
            JigsawStructure.DEFAULT_DIMENSION_PADDING,
            JigsawStructure.DEFAULT_LIQUID_SETTINGS,
            tier
         )
      );
   }
}

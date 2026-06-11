package buildcraft.energy.worldgen.datagen;

import buildcraft.energy.worldgen.structure.BCEnergyStructures;
import buildcraft.energy.worldgen.template.OilStructureTemplateBuilder;
import buildcraft.energy.worldgen.template.WaterSpringTemplateBuilder;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;

public final class BCEnergyStructureProvider implements DataProvider {
   private final FabricPackOutput output;
   private final CompletableFuture<HolderLookup.Provider> registriesFuture;

   public BCEnergyStructureProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
      this.output = output;
      this.registriesFuture = registriesFuture;
   }

   @Override
   public CompletableFuture<?> run(CachedOutput cache) {
      return this.registriesFuture.thenCompose(registry -> {
         Path dataRoot = this.output.getOutputFolder(PackOutput.Target.DATA_PACK).resolve("buildcraftenergy");
         Path structuresDir = dataRoot.resolve("structure");

         try {
            OilStructureTemplateBuilder.generateAll(structuresDir);
            WaterSpringTemplateBuilder.generate(structuresDir);
         } catch (java.io.IOException e) {
            return CompletableFuture.failedFuture(e);
         }

         Structure normal = registry.lookupOrThrow(Registries.STRUCTURE).getOrThrow(BCEnergyStructures.OIL_DEPOSIT_NORMAL).value();
         Structure waterSpring = registry.lookupOrThrow(Registries.STRUCTURE).getOrThrow(BCEnergyStructures.WATER_SPRING).value();
         Structure patchDesert = registry.lookupOrThrow(Registries.STRUCTURE).getOrThrow(BCEnergyStructures.OIL_DEPOSIT_PATCH_DESERT).value();
         Structure patchOcean = registry.lookupOrThrow(Registries.STRUCTURE).getOrThrow(BCEnergyStructures.OIL_DEPOSIT_PATCH_OCEAN).value();

         StructureTemplatePool normalPool = registry.lookupOrThrow(Registries.TEMPLATE_POOL)
            .getOrThrow(BCEnergyTemplatePoolsBootstrap.NORMAL_START)
            .value();
         StructureTemplatePool patchDesertPool = registry.lookupOrThrow(Registries.TEMPLATE_POOL)
            .getOrThrow(BCEnergyTemplatePoolsBootstrap.PATCH_DESERT_START)
            .value();
         StructureTemplatePool patchOceanPool = registry.lookupOrThrow(Registries.TEMPLATE_POOL)
            .getOrThrow(BCEnergyTemplatePoolsBootstrap.PATCH_OCEAN_START)
            .value();
         StructureTemplatePool waterSpringPool = registry.lookupOrThrow(Registries.TEMPLATE_POOL)
            .getOrThrow(BCEnergyTemplatePoolsBootstrap.WATER_SPRING_START)
            .value();

         StructureSet normalSet = registry.lookupOrThrow(Registries.STRUCTURE_SET)
            .getOrThrow(BCEnergyStructures.OIL_DEPOSIT_NORMAL_SET)
            .value();
         StructureSet patchDesertSet = registry.lookupOrThrow(Registries.STRUCTURE_SET)
            .getOrThrow(BCEnergyStructures.OIL_DEPOSIT_PATCH_DESERT_SET)
            .value();
         StructureSet patchOceanSet = registry.lookupOrThrow(Registries.STRUCTURE_SET)
            .getOrThrow(BCEnergyStructures.OIL_DEPOSIT_PATCH_OCEAN_SET)
            .value();
         StructureSet patchDesertDenseSet = registry.lookupOrThrow(Registries.STRUCTURE_SET)
            .getOrThrow(BCEnergyStructures.OIL_DEPOSIT_PATCH_DESERT_DENSE_SET)
            .value();
         StructureSet patchOceanDenseSet = registry.lookupOrThrow(Registries.STRUCTURE_SET)
            .getOrThrow(BCEnergyStructures.OIL_DEPOSIT_PATCH_OCEAN_DENSE_SET)
            .value();
         StructureSet waterSpringSet = registry.lookupOrThrow(Registries.STRUCTURE_SET)
            .getOrThrow(BCEnergyStructures.WATER_SPRING_SET)
            .value();

         List<CompletableFuture<?>> saves = new ArrayList<>();
         saves.add(
            DataProvider.saveStable(cache, registry, Structure.DIRECT_CODEC, normal, dataRoot.resolve("worldgen/structure/oil_deposit_normal.json"))
         );
         saves.add(
            DataProvider.saveStable(
               cache, registry, Structure.DIRECT_CODEC, patchDesert, dataRoot.resolve("worldgen/structure/oil_deposit_patch_desert.json")
            )
         );
         saves.add(
            DataProvider.saveStable(
               cache, registry, Structure.DIRECT_CODEC, patchOcean, dataRoot.resolve("worldgen/structure/oil_deposit_patch_ocean.json")
            )
         );
         saves.add(
            DataProvider.saveStable(
               cache, registry, StructureTemplatePool.DIRECT_CODEC, normalPool, dataRoot.resolve("worldgen/template_pool/oil_deposit_normal/start.json")
            )
         );
         saves.add(
            DataProvider.saveStable(
               cache,
               registry,
               StructureTemplatePool.DIRECT_CODEC,
               patchDesertPool,
               dataRoot.resolve("worldgen/template_pool/oil_deposit_patch_desert/start.json")
            )
         );
         saves.add(
            DataProvider.saveStable(
               cache,
               registry,
               StructureTemplatePool.DIRECT_CODEC,
               patchOceanPool,
               dataRoot.resolve("worldgen/template_pool/oil_deposit_patch_ocean/start.json")
            )
         );
         saves.add(
            DataProvider.saveStable(
               cache, registry, StructureSet.DIRECT_CODEC, normalSet, dataRoot.resolve("worldgen/structure_set/oil_deposit_normal.json")
            )
         );
         saves.add(
            DataProvider.saveStable(
               cache, registry, StructureSet.DIRECT_CODEC, patchDesertSet, dataRoot.resolve("worldgen/structure_set/oil_deposit_patch_desert.json")
            )
         );
         saves.add(
            DataProvider.saveStable(
               cache, registry, StructureSet.DIRECT_CODEC, patchOceanSet, dataRoot.resolve("worldgen/structure_set/oil_deposit_patch_ocean.json")
            )
         );
         saves.add(
            DataProvider.saveStable(
               cache,
               registry,
               StructureSet.DIRECT_CODEC,
               patchDesertDenseSet,
               dataRoot.resolve("worldgen/structure_set/oil_deposit_patch_desert_dense.json")
            )
         );
         saves.add(
            DataProvider.saveStable(
               cache,
               registry,
               StructureSet.DIRECT_CODEC,
               patchOceanDenseSet,
               dataRoot.resolve("worldgen/structure_set/oil_deposit_patch_ocean_dense.json")
            )
         );
         saves.add(
            DataProvider.saveStable(cache, registry, Structure.DIRECT_CODEC, waterSpring, dataRoot.resolve("worldgen/structure/water_spring.json"))
         );
         saves.add(
            DataProvider.saveStable(
               cache,
               registry,
               StructureTemplatePool.DIRECT_CODEC,
               waterSpringPool,
               dataRoot.resolve("worldgen/template_pool/water_spring/start.json")
            )
         );
         saves.add(
            DataProvider.saveStable(
               cache, registry, StructureSet.DIRECT_CODEC, waterSpringSet, dataRoot.resolve("worldgen/structure_set/water_spring.json")
            )
         );

         for (ResourceKey<StructureProcessorList> processorKey : BCEnergyProcessorListsBootstrap.ALL) {
            StructureProcessorList processors = registry.lookupOrThrow(Registries.PROCESSOR_LIST).getOrThrow(processorKey).value();
            saves.add(
               DataProvider.saveStable(
                  cache,
                  registry,
                  StructureProcessorType.DIRECT_CODEC,
                  processors,
                  dataRoot.resolve("worldgen/processor_list/" + processorKey.identifier().getPath() + ".json")
               )
            );
         }

         return CompletableFuture.allOf(saves.toArray(CompletableFuture[]::new));
      });
   }

   @Override
   public String getName() {
      return "BuildCraft Energy Structures";
   }
}

package buildcraft.energy.worldgen.datagen;

import buildcraft.energy.worldgen.structure.BCEnergyStructures;
import buildcraft.energy.worldgen.structure.OilStructureTemplateBuilder;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

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
         } catch (java.io.IOException e) {
            return CompletableFuture.failedFuture(e);
         }

         Structure normal = registry.lookupOrThrow(Registries.STRUCTURE).getOrThrow(BCEnergyStructures.OIL_DEPOSIT_NORMAL).value();
         Structure rich = registry.lookupOrThrow(Registries.STRUCTURE).getOrThrow(BCEnergyStructures.OIL_DEPOSIT_RICH).value();
         Structure patchDesert = registry.lookupOrThrow(Registries.STRUCTURE).getOrThrow(BCEnergyStructures.OIL_DEPOSIT_PATCH_DESERT).value();
         Structure patchOcean = registry.lookupOrThrow(Registries.STRUCTURE).getOrThrow(BCEnergyStructures.OIL_DEPOSIT_PATCH_OCEAN).value();

         StructureTemplatePool normalPool = registry.lookupOrThrow(Registries.TEMPLATE_POOL)
            .getOrThrow(BCEnergyTemplatePoolsBootstrap.NORMAL_START)
            .value();
         StructureTemplatePool richPool = registry.lookupOrThrow(Registries.TEMPLATE_POOL)
            .getOrThrow(BCEnergyTemplatePoolsBootstrap.RICH_START)
            .value();
         StructureTemplatePool patchPool = registry.lookupOrThrow(Registries.TEMPLATE_POOL)
            .getOrThrow(BCEnergyTemplatePoolsBootstrap.PATCH_START)
            .value();

         StructureSet normalSet = registry.lookupOrThrow(Registries.STRUCTURE_SET)
            .getOrThrow(BCEnergyStructures.OIL_DEPOSIT_NORMAL_SET)
            .value();
         StructureSet richSet = registry.lookupOrThrow(Registries.STRUCTURE_SET)
            .getOrThrow(BCEnergyStructures.OIL_DEPOSIT_RICH_SET)
            .value();
         StructureSet patchDesertSet = registry.lookupOrThrow(Registries.STRUCTURE_SET)
            .getOrThrow(BCEnergyStructures.OIL_DEPOSIT_PATCH_DESERT_SET)
            .value();
         StructureSet patchOceanSet = registry.lookupOrThrow(Registries.STRUCTURE_SET)
            .getOrThrow(BCEnergyStructures.OIL_DEPOSIT_PATCH_OCEAN_SET)
            .value();

         return CompletableFuture.allOf(
            DataProvider.saveStable(cache, registry, Structure.DIRECT_CODEC, normal, dataRoot.resolve("worldgen/structure/oil_deposit_normal.json")),
            DataProvider.saveStable(cache, registry, Structure.DIRECT_CODEC, rich, dataRoot.resolve("worldgen/structure/oil_deposit_rich.json")),
            DataProvider.saveStable(
               cache, registry, Structure.DIRECT_CODEC, patchDesert, dataRoot.resolve("worldgen/structure/oil_deposit_patch_desert.json")
            ),
            DataProvider.saveStable(
               cache, registry, Structure.DIRECT_CODEC, patchOcean, dataRoot.resolve("worldgen/structure/oil_deposit_patch_ocean.json")
            ),
            DataProvider.saveStable(
               cache, registry, StructureTemplatePool.DIRECT_CODEC, normalPool, dataRoot.resolve("worldgen/template_pool/oil_deposit_normal/start.json")
            ),
            DataProvider.saveStable(
               cache, registry, StructureTemplatePool.DIRECT_CODEC, richPool, dataRoot.resolve("worldgen/template_pool/oil_deposit_rich/start.json")
            ),
            DataProvider.saveStable(
               cache, registry, StructureTemplatePool.DIRECT_CODEC, patchPool, dataRoot.resolve("worldgen/template_pool/oil_deposit_patch/start.json")
            ),
            DataProvider.saveStable(
               cache, registry, StructureSet.DIRECT_CODEC, normalSet, dataRoot.resolve("worldgen/structure_set/oil_deposit_normal.json")
            ),
            DataProvider.saveStable(
               cache, registry, StructureSet.DIRECT_CODEC, richSet, dataRoot.resolve("worldgen/structure_set/oil_deposit_rich.json")
            ),
            DataProvider.saveStable(
               cache, registry, StructureSet.DIRECT_CODEC, patchDesertSet, dataRoot.resolve("worldgen/structure_set/oil_deposit_patch_desert.json")
            ),
            DataProvider.saveStable(
               cache, registry, StructureSet.DIRECT_CODEC, patchOceanSet, dataRoot.resolve("worldgen/structure_set/oil_deposit_patch_ocean.json")
            )
         );
      });
   }

   @Override
   public String getName() {
      return "BuildCraft Energy Structures";
   }
}

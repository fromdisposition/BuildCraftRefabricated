package buildcraft.energy.worldgen.datagen;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import buildcraft.energy.worldgen.structure.OilDepositPoolElement;
import buildcraft.fabric.BCRegistries;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.resources.ResourceKey;
import java.util.function.Function;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

final class BCEnergyTemplatePoolsBootstrap {
   static final ResourceKey<StructureTemplatePool> NORMAL_START = ResourceKey.create(
      Registries.TEMPLATE_POOL, BCRegistries.id("buildcraftenergy", "oil_deposit_normal/start")
   );
   static final ResourceKey<StructureTemplatePool> PATCH_DESERT_START = ResourceKey.create(
      Registries.TEMPLATE_POOL, BCRegistries.id("buildcraftenergy", "oil_deposit_patch_desert/start")
   );
   static final ResourceKey<StructureTemplatePool> PATCH_OCEAN_START = ResourceKey.create(
      Registries.TEMPLATE_POOL, BCRegistries.id("buildcraftenergy", "oil_deposit_patch_ocean/start")
   );

   private BCEnergyTemplatePoolsBootstrap() {
   }

   static void bootstrap(BootstrapContext<StructureTemplatePool> context) {
      HolderGetter<StructureTemplatePool> pools = context.lookup(Registries.TEMPLATE_POOL);
      HolderGetter<StructureProcessorList> processors = context.lookup(Registries.PROCESSOR_LIST);
      Holder<StructureTemplatePool> empty = pools.getOrThrow(Pools.EMPTY);
      Holder<StructureProcessorList> surfaceGravity = processors.getOrThrow(BCEnergyProcessorListsBootstrap.OIL_SURFACE_GRAVITY);

      context.register(
         NORMAL_START,
         new StructureTemplatePool(
            empty,
            ImmutableList.of(
               Pair.of(well(processors, "oil_well_large", BCEnergyProcessorListsBootstrap.OIL_WELL_LARGE), 8),
               Pair.of(well(processors, "oil_well_large_s", BCEnergyProcessorListsBootstrap.OIL_WELL_LARGE_S), 6),
               Pair.of(well(processors, "oil_well_large_m", BCEnergyProcessorListsBootstrap.OIL_WELL_LARGE_M), 6),
               Pair.of(well(processors, "oil_well_medium", BCEnergyProcessorListsBootstrap.OIL_WELL_MEDIUM), 20),
               Pair.of(well(processors, "oil_well_medium_l", BCEnergyProcessorListsBootstrap.OIL_WELL_MEDIUM_L), 15),
               Pair.of(well(processors, "oil_well_medium_alt", BCEnergyProcessorListsBootstrap.OIL_WELL_MEDIUM_ALT), 12),
               Pair.of(well(processors, "oil_well_medium_s", BCEnergyProcessorListsBootstrap.OIL_WELL_MEDIUM_S), 13)
            ),
            StructureTemplatePool.Projection.RIGID
         )
      );

      context.register(
         PATCH_DESERT_START,
         new StructureTemplatePool(
            empty,
            ImmutableList.of(
               Pair.of(well(processors, "oil_well_large", BCEnergyProcessorListsBootstrap.OIL_WELL_LARGE), 8),
               Pair.of(well(processors, "oil_well_large_s", BCEnergyProcessorListsBootstrap.OIL_WELL_LARGE_S), 6),
               Pair.of(well(processors, "oil_well_large_m", BCEnergyProcessorListsBootstrap.OIL_WELL_LARGE_M), 3),
               Pair.of(well(processors, "oil_well_large_l", BCEnergyProcessorListsBootstrap.OIL_WELL_LARGE_L), 3),
               Pair.of(well(processors, "oil_well_medium", BCEnergyProcessorListsBootstrap.OIL_WELL_MEDIUM), 25),
               Pair.of(well(processors, "oil_well_medium_l", BCEnergyProcessorListsBootstrap.OIL_WELL_MEDIUM_L), 15),
               Pair.of(well(processors, "oil_well_medium_alt", BCEnergyProcessorListsBootstrap.OIL_WELL_MEDIUM_ALT), 12),
               Pair.of(well(processors, "oil_well_medium_s", BCEnergyProcessorListsBootstrap.OIL_WELL_MEDIUM_S), 8),
               Pair.of(lake(surfaceGravity, "oil_lake_patch"), 4),
               Pair.of(lake(surfaceGravity, "oil_lake_patch_b"), 4),
               Pair.of(lake(surfaceGravity, "oil_lake_patch_c"), 4),
               Pair.of(lake(surfaceGravity, "oil_lake_patch_d"), 4),
               Pair.of(lake(surfaceGravity, "oil_lake_patch_e"), 4)
            ),
            StructureTemplatePool.Projection.RIGID
         )
      );

      context.register(
         PATCH_OCEAN_START,
         new StructureTemplatePool(
            empty,
            ImmutableList.of(
               Pair.of(well(processors, "oil_well_large", BCEnergyProcessorListsBootstrap.OIL_WELL_LARGE), 8),
               Pair.of(well(processors, "oil_well_large_s", BCEnergyProcessorListsBootstrap.OIL_WELL_LARGE_S), 6),
               Pair.of(well(processors, "oil_well_large_m", BCEnergyProcessorListsBootstrap.OIL_WELL_LARGE_M), 3),
               Pair.of(well(processors, "oil_well_large_l", BCEnergyProcessorListsBootstrap.OIL_WELL_LARGE_L), 3),
               Pair.of(well(processors, "oil_well_medium", BCEnergyProcessorListsBootstrap.OIL_WELL_MEDIUM), 25),
               Pair.of(well(processors, "oil_well_medium_l", BCEnergyProcessorListsBootstrap.OIL_WELL_MEDIUM_L), 15),
               Pair.of(well(processors, "oil_well_medium_alt", BCEnergyProcessorListsBootstrap.OIL_WELL_MEDIUM_ALT), 12),
               Pair.of(well(processors, "oil_well_medium_s", BCEnergyProcessorListsBootstrap.OIL_WELL_MEDIUM_S), 8),
               Pair.of(lake(surfaceGravity, "oil_lake_patch"), 4),
               Pair.of(lake(surfaceGravity, "oil_lake_patch_b"), 4),
               Pair.of(lake(surfaceGravity, "oil_lake_patch_c"), 4),
               Pair.of(lake(surfaceGravity, "oil_lake_patch_d"), 4),
               Pair.of(lake(surfaceGravity, "oil_lake_patch_e"), 4)
            ),
            StructureTemplatePool.Projection.RIGID
         )
      );
   }

   private static Function<StructureTemplatePool.Projection, ? extends StructurePoolElement> well(
      HolderGetter<StructureProcessorList> processors,
      String template,
      ResourceKey<StructureProcessorList> processorKey
   ) {
      return OilDepositPoolElement.of("buildcraftenergy:" + template, processors.getOrThrow(processorKey));
   }

   private static Function<StructureTemplatePool.Projection, ? extends StructurePoolElement> lake(
      Holder<StructureProcessorList> surfaceGravity,
      String template
   ) {
      return OilDepositPoolElement.of("buildcraftenergy:" + template, surfaceGravity);
   }
}

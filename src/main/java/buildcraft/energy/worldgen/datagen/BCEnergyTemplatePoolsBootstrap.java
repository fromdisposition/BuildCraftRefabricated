package buildcraft.energy.worldgen.datagen;

import buildcraft.energy.worldgen.structure.OilDepositPoolElement;
import buildcraft.energy.worldgen.structure.WaterSpringPoolElement;
import buildcraft.fabric.BCRegistries;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.resources.ResourceKey;
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
   static final ResourceKey<StructureTemplatePool> WATER_SPRING_START = ResourceKey.create(
      Registries.TEMPLATE_POOL, BCRegistries.id("buildcraftenergy", "water_spring/start")
   );

   private BCEnergyTemplatePoolsBootstrap() {
   }

   static void bootstrap(BootstrapContext<StructureTemplatePool> context) {
      HolderGetter<StructureTemplatePool> pools = context.lookup(Registries.TEMPLATE_POOL);
      HolderGetter<StructureProcessorList> processors = context.lookup(Registries.PROCESSOR_LIST);
      Holder<StructureTemplatePool> empty = pools.getOrThrow(Pools.EMPTY);
      Holder<StructureProcessorList> oilWell = processors.getOrThrow(BCEnergyProcessorListsBootstrap.OIL_WELL);
      Holder<StructureProcessorList> surfaceGravity = processors.getOrThrow(BCEnergyProcessorListsBootstrap.OIL_SURFACE_GRAVITY);

      context.register(
         NORMAL_START,
         new StructureTemplatePool(
            empty,
            ImmutableList.of(
               Pair.of(well(oilWell, "oil_well_large"), 8),
               Pair.of(well(oilWell, "oil_well_large_s"), 6),
               Pair.of(well(oilWell, "oil_well_large_m"), 6),
               Pair.of(well(oilWell, "oil_well_medium"), 20),
               Pair.of(well(oilWell, "oil_well_medium_l"), 15),
               Pair.of(well(oilWell, "oil_well_medium_alt"), 12),
               Pair.of(well(oilWell, "oil_well_medium_s"), 13)
            ),
            StructureTemplatePool.Projection.RIGID
         )
      );

      context.register(
         PATCH_DESERT_START,
         new StructureTemplatePool(empty, patchDesertElements(oilWell, surfaceGravity), StructureTemplatePool.Projection.RIGID)
      );

      context.register(
         PATCH_OCEAN_START,
         new StructureTemplatePool(empty, patchOceanElements(oilWell, surfaceGravity), StructureTemplatePool.Projection.RIGID)
      );

      context.register(
         WATER_SPRING_START,
         new StructureTemplatePool(
            empty,
            ImmutableList.of(
               Pair.of(
                  WaterSpringPoolElement.of("buildcraftenergy:water_spring", processors.getOrThrow(BCEnergyProcessorListsBootstrap.WATER_SPRING_BEDROCK)),
                  1
               )
            ),
            StructureTemplatePool.Projection.RIGID
         )
      );
   }

   private static ImmutableList<Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer>> patchDesertElements(
      Holder<StructureProcessorList> oilWell,
      Holder<StructureProcessorList> surfaceGravity
   ) {
      return ImmutableList.of(
         Pair.of(well(oilWell, "oil_well_large"), 8),
         Pair.of(well(oilWell, "oil_well_large_s"), 6),
         Pair.of(well(oilWell, "oil_well_large_m"), 3),
         Pair.of(well(oilWell, "oil_well_large_l"), 3),
         Pair.of(well(oilWell, "oil_well_medium"), 25),
         Pair.of(well(oilWell, "oil_well_medium_l"), 15),
         Pair.of(well(oilWell, "oil_well_medium_alt"), 12),
         Pair.of(well(oilWell, "oil_well_medium_s"), 8),
         Pair.of(lake(surfaceGravity, "oil_lake_patch"), 10),
         Pair.of(lake(surfaceGravity, "oil_lake_patch_b"), 10),
         Pair.of(lake(surfaceGravity, "oil_lake_patch_c"), 10),
         Pair.of(lake(surfaceGravity, "oil_lake_patch_d"), 10),
         Pair.of(lake(surfaceGravity, "oil_lake_patch_e"), 10)
      );
   }

   private static ImmutableList<Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer>> patchOceanElements(
      Holder<StructureProcessorList> oilWell,
      Holder<StructureProcessorList> surfaceGravity
   ) {
      return ImmutableList.of(
         Pair.of(well(oilWell, "oil_well_large"), 8),
         Pair.of(well(oilWell, "oil_well_large_s"), 6),
         Pair.of(well(oilWell, "oil_well_large_m"), 3),
         Pair.of(well(oilWell, "oil_well_large_l"), 3),
         Pair.of(well(oilWell, "oil_well_medium"), 25),
         Pair.of(well(oilWell, "oil_well_medium_l"), 15),
         Pair.of(well(oilWell, "oil_well_medium_alt"), 12),
         Pair.of(well(oilWell, "oil_well_medium_s"), 8),
         Pair.of(lake(surfaceGravity, "oil_lake_patch"), 10),
         Pair.of(lake(surfaceGravity, "oil_lake_patch_b"), 10),
         Pair.of(lake(surfaceGravity, "oil_lake_patch_c"), 10),
         Pair.of(lake(surfaceGravity, "oil_lake_patch_d"), 10),
         Pair.of(lake(surfaceGravity, "oil_lake_patch_e"), 10)
      );
   }

   private static Function<StructureTemplatePool.Projection, ? extends StructurePoolElement> well(
      Holder<StructureProcessorList> oilWell,
      String template
   ) {
      return OilDepositPoolElement.of("buildcraftenergy:" + template, oilWell);
   }

   private static Function<StructureTemplatePool.Projection, ? extends StructurePoolElement> lake(
      Holder<StructureProcessorList> gravity,
      String template
   ) {
      return OilDepositPoolElement.of("buildcraftenergy:" + template, gravity);
   }
}

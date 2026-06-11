package buildcraft.energy.worldgen.datagen;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import buildcraft.fabric.BCRegistries;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

final class BCEnergyTemplatePoolsBootstrap {
   static final ResourceKey<StructureTemplatePool> NORMAL_START = ResourceKey.create(
      Registries.TEMPLATE_POOL, BCRegistries.id("buildcraftenergy", "oil_deposit_normal/start")
   );
   static final ResourceKey<StructureTemplatePool> RICH_START = ResourceKey.create(
      Registries.TEMPLATE_POOL, BCRegistries.id("buildcraftenergy", "oil_deposit_rich/start")
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
      Holder<StructureTemplatePool> empty = pools.getOrThrow(Pools.EMPTY);

      context.register(
         NORMAL_START,
         new StructureTemplatePool(
            empty,
            ImmutableList.of(
               Pair.of(StructurePoolElement.single("buildcraftenergy:oil_well_large"), 8),
               Pair.of(StructurePoolElement.single("buildcraftenergy:oil_well_large_s"), 6),
               Pair.of(StructurePoolElement.single("buildcraftenergy:oil_well_large_m"), 6),
               Pair.of(StructurePoolElement.single("buildcraftenergy:oil_well_medium"), 20),
               Pair.of(StructurePoolElement.single("buildcraftenergy:oil_well_medium_l"), 15),
               Pair.of(StructurePoolElement.single("buildcraftenergy:oil_well_medium_alt"), 12),
               Pair.of(StructurePoolElement.single("buildcraftenergy:oil_well_medium_s"), 13)
            ),
            StructureTemplatePool.Projection.TERRAIN_MATCHING
         )
      );

      context.register(
         RICH_START,
         new StructureTemplatePool(
            empty,
            ImmutableList.of(
               Pair.of(StructurePoolElement.single("buildcraftenergy:oil_well_large"), 8),
               Pair.of(StructurePoolElement.single("buildcraftenergy:oil_well_large_s"), 6),
               Pair.of(StructurePoolElement.single("buildcraftenergy:oil_well_large_m"), 3),
               Pair.of(StructurePoolElement.single("buildcraftenergy:oil_well_large_l"), 3),
               Pair.of(StructurePoolElement.single("buildcraftenergy:oil_well_medium"), 25),
               Pair.of(StructurePoolElement.single("buildcraftenergy:oil_well_medium_l"), 15),
               Pair.of(StructurePoolElement.single("buildcraftenergy:oil_well_medium_alt"), 12),
               Pair.of(StructurePoolElement.single("buildcraftenergy:oil_well_medium_s"), 8),
               Pair.of(StructurePoolElement.single("buildcraftenergy:oil_lake_patch"), 4),
               Pair.of(StructurePoolElement.single("buildcraftenergy:oil_lake_patch_b"), 4),
               Pair.of(StructurePoolElement.single("buildcraftenergy:oil_lake_patch_c"), 4),
               Pair.of(StructurePoolElement.single("buildcraftenergy:oil_lake_patch_d"), 4),
               Pair.of(StructurePoolElement.single("buildcraftenergy:oil_lake_patch_e"), 4)
            ),
            StructureTemplatePool.Projection.TERRAIN_MATCHING
         )
      );

      context.register(
         PATCH_DESERT_START,
         new StructureTemplatePool(
            empty,
            ImmutableList.of(
               Pair.of(StructurePoolElement.single("buildcraftenergy:oil_well_large"), 8),
               Pair.of(StructurePoolElement.single("buildcraftenergy:oil_well_large_s"), 6),
               Pair.of(StructurePoolElement.single("buildcraftenergy:oil_well_large_m"), 3),
               Pair.of(StructurePoolElement.single("buildcraftenergy:oil_well_large_l"), 3),
               Pair.of(StructurePoolElement.single("buildcraftenergy:oil_well_medium"), 25),
               Pair.of(StructurePoolElement.single("buildcraftenergy:oil_well_medium_l"), 15),
               Pair.of(StructurePoolElement.single("buildcraftenergy:oil_well_medium_alt"), 12),
               Pair.of(StructurePoolElement.single("buildcraftenergy:oil_well_medium_s"), 8),
               Pair.of(StructurePoolElement.single("buildcraftenergy:oil_lake_patch"), 4),
               Pair.of(StructurePoolElement.single("buildcraftenergy:oil_lake_patch_b"), 4),
               Pair.of(StructurePoolElement.single("buildcraftenergy:oil_lake_patch_c"), 4),
               Pair.of(StructurePoolElement.single("buildcraftenergy:oil_lake_patch_d"), 4),
               Pair.of(StructurePoolElement.single("buildcraftenergy:oil_lake_patch_e"), 4)
            ),
            StructureTemplatePool.Projection.TERRAIN_MATCHING
         )
      );

      context.register(
         PATCH_OCEAN_START,
         new StructureTemplatePool(
            empty,
            ImmutableList.of(
               Pair.of(StructurePoolElement.single("buildcraftenergy:oil_well_large"), 8),
               Pair.of(StructurePoolElement.single("buildcraftenergy:oil_well_large_s"), 6),
               Pair.of(StructurePoolElement.single("buildcraftenergy:oil_well_large_m"), 3),
               Pair.of(StructurePoolElement.single("buildcraftenergy:oil_well_large_l"), 3),
               Pair.of(StructurePoolElement.single("buildcraftenergy:oil_well_medium"), 25),
               Pair.of(StructurePoolElement.single("buildcraftenergy:oil_well_medium_l"), 15),
               Pair.of(StructurePoolElement.single("buildcraftenergy:oil_well_medium_alt"), 12),
               Pair.of(StructurePoolElement.single("buildcraftenergy:oil_well_medium_s"), 8),
               Pair.of(StructurePoolElement.single("buildcraftenergy:oil_lake_patch"), 4),
               Pair.of(StructurePoolElement.single("buildcraftenergy:oil_lake_patch_b"), 4),
               Pair.of(StructurePoolElement.single("buildcraftenergy:oil_lake_patch_c"), 4),
               Pair.of(StructurePoolElement.single("buildcraftenergy:oil_lake_patch_d"), 4),
               Pair.of(StructurePoolElement.single("buildcraftenergy:oil_lake_patch_e"), 4)
            ),
            StructureTemplatePool.Projection.TERRAIN_MATCHING
         )
      );
   }
}

package buildcraft.energy.worldgen.datagen;

import buildcraft.energy.worldgen.structure.OilStructureDefaults;
import buildcraft.energy.worldgen.structure.processor.OilWellProjectionProcessor;
import buildcraft.fabric.BCRegistries;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.GravityProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

public final class BCEnergyProcessorListsBootstrap {
   public static final ResourceKey<StructureProcessorList> OIL_SURFACE_GRAVITY = ResourceKey.create(
      Registries.PROCESSOR_LIST, BCRegistries.id("buildcraftenergy", "oil_surface_gravity")
   );

   public static final ResourceKey<StructureProcessorList> OIL_WELL_MEDIUM_S = wellKey("oil_well_medium_s", 20, 4);
   public static final ResourceKey<StructureProcessorList> OIL_WELL_MEDIUM_ALT = wellKey("oil_well_medium_alt", 24, 5);
   public static final ResourceKey<StructureProcessorList> OIL_WELL_MEDIUM = wellKey("oil_well_medium", 26, 6);
   public static final ResourceKey<StructureProcessorList> OIL_WELL_MEDIUM_L = wellKey("oil_well_medium_l", 28, 7);
   public static final ResourceKey<StructureProcessorList> OIL_WELL_LARGE_S = wellKey("oil_well_large_s", 24, 10);
   public static final ResourceKey<StructureProcessorList> OIL_WELL_LARGE_M = wellKey("oil_well_large_m", 27, 12);
   public static final ResourceKey<StructureProcessorList> OIL_WELL_LARGE = wellKey("oil_well_large", 28, 14);
   public static final ResourceKey<StructureProcessorList> OIL_WELL_LARGE_L = wellKey("oil_well_large_l", 29, 16);

   public static final List<ResourceKey<StructureProcessorList>> ALL = List.of(
      OIL_SURFACE_GRAVITY,
      OIL_WELL_MEDIUM_S,
      OIL_WELL_MEDIUM_ALT,
      OIL_WELL_MEDIUM,
      OIL_WELL_MEDIUM_L,
      OIL_WELL_LARGE_S,
      OIL_WELL_LARGE_M,
      OIL_WELL_LARGE,
      OIL_WELL_LARGE_L
   );

   private BCEnergyProcessorListsBootstrap() {
   }

   private static ResourceKey<StructureProcessorList> wellKey(String id, int sphereCenterOffset, int sphereRadius) {
      return ResourceKey.create(Registries.PROCESSOR_LIST, BCRegistries.id("buildcraftenergy", id));
   }

   static void bootstrap(BootstrapContext<StructureProcessorList> context) {
      context.register(
         OIL_SURFACE_GRAVITY,
         new StructureProcessorList(List.of(new GravityProcessor(Heightmap.Types.WORLD_SURFACE_WG, -1)))
      );

      registerWell(context, OIL_WELL_MEDIUM_S, 20, 4);
      registerWell(context, OIL_WELL_MEDIUM_ALT, 24, 5);
      registerWell(context, OIL_WELL_MEDIUM, 26, 6);
      registerWell(context, OIL_WELL_MEDIUM_L, 28, 7);
      registerWell(context, OIL_WELL_LARGE_S, 24, 10);
      registerWell(context, OIL_WELL_LARGE_M, 27, 12);
      registerWell(context, OIL_WELL_LARGE, 28, 14);
      registerWell(context, OIL_WELL_LARGE_L, 29, 16);
   }

   private static void registerWell(
      BootstrapContext<StructureProcessorList> context,
      ResourceKey<StructureProcessorList> key,
      int sphereCenterOffset,
      int sphereRadius
   ) {
      context.register(
         key,
         new StructureProcessorList(
            List.of(new OilWellProjectionProcessor(sphereCenterOffset, sphereRadius, OilStructureDefaults.SPHERE_TEMPLATE_CENTER_Y))
         )
      );
   }
}

package buildcraft.energy.worldgen.datagen;

import buildcraft.energy.worldgen.processor.OilWellProjectionProcessor;
import buildcraft.energy.worldgen.processor.WaterSpringBedrockProcessor;
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
   public static final ResourceKey<StructureProcessorList> WATER_SPRING_BEDROCK = ResourceKey.create(
      Registries.PROCESSOR_LIST, BCRegistries.id("buildcraftenergy", "water_spring_bedrock")
   );

   public static final ResourceKey<StructureProcessorList> OIL_WELL_MEDIUM_S = wellKey("oil_well_medium_s");
   public static final ResourceKey<StructureProcessorList> OIL_WELL_MEDIUM_ALT = wellKey("oil_well_medium_alt");
   public static final ResourceKey<StructureProcessorList> OIL_WELL_MEDIUM = wellKey("oil_well_medium");
   public static final ResourceKey<StructureProcessorList> OIL_WELL_MEDIUM_L = wellKey("oil_well_medium_l");
   public static final ResourceKey<StructureProcessorList> OIL_WELL_LARGE_S = wellKey("oil_well_large_s");
   public static final ResourceKey<StructureProcessorList> OIL_WELL_LARGE_M = wellKey("oil_well_large_m");
   public static final ResourceKey<StructureProcessorList> OIL_WELL_LARGE = wellKey("oil_well_large");
   public static final ResourceKey<StructureProcessorList> OIL_WELL_LARGE_L = wellKey("oil_well_large_l");

   public static final List<ResourceKey<StructureProcessorList>> ALL = List.of(
      OIL_SURFACE_GRAVITY,
      WATER_SPRING_BEDROCK,
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

   private static ResourceKey<StructureProcessorList> wellKey(String id) {
      return ResourceKey.create(Registries.PROCESSOR_LIST, BCRegistries.id("buildcraftenergy", id));
   }

   private static StructureProcessorList wellProcessors() {
      return new StructureProcessorList(
         List.of(new GravityProcessor(Heightmap.Types.WORLD_SURFACE_WG, -1), new OilWellProjectionProcessor())
      );
   }

   static void bootstrap(BootstrapContext<StructureProcessorList> context) {
      context.register(
         OIL_SURFACE_GRAVITY,
         new StructureProcessorList(List.of(new GravityProcessor(Heightmap.Types.WORLD_SURFACE_WG, -1)))
      );
      context.register(WATER_SPRING_BEDROCK, new StructureProcessorList(List.of(new WaterSpringBedrockProcessor())));

      StructureProcessorList wells = wellProcessors();
      context.register(OIL_WELL_MEDIUM_S, wells);
      context.register(OIL_WELL_MEDIUM_ALT, wells);
      context.register(OIL_WELL_MEDIUM, wells);
      context.register(OIL_WELL_MEDIUM_L, wells);
      context.register(OIL_WELL_LARGE_S, wells);
      context.register(OIL_WELL_LARGE_M, wells);
      context.register(OIL_WELL_LARGE, wells);
      context.register(OIL_WELL_LARGE_L, wells);
   }
}

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
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

public final class BCEnergyProcessorListsBootstrap {
   public static final ResourceKey<StructureProcessorList> OIL_SURFACE_GRAVITY = createKey("oil_surface_gravity");
   public static final ResourceKey<StructureProcessorList> OIL_WELL = createKey("oil_well");
   public static final ResourceKey<StructureProcessorList> WATER_SPRING_BEDROCK = createKey("water_spring_bedrock");

   public static final List<ResourceKey<StructureProcessorList>> ALL = List.of(
      OIL_SURFACE_GRAVITY,
      OIL_WELL,
      WATER_SPRING_BEDROCK
   );

   private BCEnergyProcessorListsBootstrap() {
   }

   private static ResourceKey<StructureProcessorList> createKey(String id) {
      return ResourceKey.create(Registries.PROCESSOR_LIST, BCRegistries.id("buildcraftenergy", id));
   }

   private static void register(
      final BootstrapContext<StructureProcessorList> context,
      final ResourceKey<StructureProcessorList> id,
      final List<StructureProcessor> processors
   ) {
      context.register(id, new StructureProcessorList(processors));
   }

   static void bootstrap(BootstrapContext<StructureProcessorList> context) {
      register(
         context,
         OIL_SURFACE_GRAVITY,
         List.of(new GravityProcessor(Heightmap.Types.WORLD_SURFACE_WG, -1))
      );
      register(
         context,
         OIL_WELL,
         List.of(new GravityProcessor(Heightmap.Types.WORLD_SURFACE_WG, -1), new OilWellProjectionProcessor())
      );
      register(context, WATER_SPRING_BEDROCK, List.of(new WaterSpringBedrockProcessor()));
   }
}

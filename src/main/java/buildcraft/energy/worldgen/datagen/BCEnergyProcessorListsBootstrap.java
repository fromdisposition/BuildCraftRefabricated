package buildcraft.energy.worldgen.datagen;

import buildcraft.fabric.BCRegistries;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.GravityProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

public final class BCEnergyProcessorListsBootstrap {
   public static final ResourceKey<StructureProcessorList> OIL_OCEAN_FLOOR = ResourceKey.create(
      Registries.PROCESSOR_LIST, BCRegistries.id("buildcraftenergy", "oil_ocean_floor")
   );

   private BCEnergyProcessorListsBootstrap() {
   }

   static void bootstrap(BootstrapContext<StructureProcessorList> context) {
      context.register(
         OIL_OCEAN_FLOOR,
         new StructureProcessorList(List.of(new GravityProcessor(Heightmap.Types.OCEAN_FLOOR_WG, -1)))
      );
   }
}

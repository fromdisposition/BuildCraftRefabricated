package buildcraft.energy.worldgen.structure;

import buildcraft.energy.worldgen.structure.processor.OilWellProjectionProcessor;
import buildcraft.fabric.BCRegistries;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;

public final class BCEnergyStructureProcessorTypes {
   public static StructureProcessorType<OilWellProjectionProcessor> OIL_WELL_PROJECTION;

   private BCEnergyStructureProcessorTypes() {
   }

   public static void register() {
      if (OIL_WELL_PROJECTION == null) {
         OIL_WELL_PROJECTION = Registry.register(
            BuiltInRegistries.STRUCTURE_PROCESSOR,
            BCRegistries.id("buildcraftenergy", "oil_well_projection"),
            () -> OilWellProjectionProcessor.CODEC
         );
      }
   }
}

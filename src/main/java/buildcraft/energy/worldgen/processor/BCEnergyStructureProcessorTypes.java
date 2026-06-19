package buildcraft.energy.worldgen.processor;
import buildcraft.fabric.BCRegistries;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
//? if >= 26.2 {
//?} else {
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
//?}

public final class BCEnergyStructureProcessorTypes {
   //? if >= 26.2 {
   /*private static boolean registered;
   *///?} else {
   @SuppressWarnings("rawtypes")
   public static StructureProcessorType<OilWellProjectionProcessor> OIL_WELL_PROJECTION;
   @SuppressWarnings("rawtypes")
   public static StructureProcessorType<WaterSpringBedrockProcessor> WATER_SPRING_BEDROCK;
   //?}

   private BCEnergyStructureProcessorTypes() {
   }

   public static void register() {
      //? if >= 26.2 {
      /*if (!registered) {
         registered = true;
         Registry.register(
            BuiltInRegistries.STRUCTURE_PROCESSOR,
            BCRegistries.id("buildcraftenergy", "oil_well_projection"),
            OilWellProjectionProcessor.CODEC
         );
         Registry.register(
            BuiltInRegistries.STRUCTURE_PROCESSOR,
            BCRegistries.id("buildcraftenergy", "water_spring_bedrock"),
            WaterSpringBedrockProcessor.CODEC
         );
      }
      *///?} else {
      if (OIL_WELL_PROJECTION == null) {
         OIL_WELL_PROJECTION = Registry.register(
            BuiltInRegistries.STRUCTURE_PROCESSOR,
            BCRegistries.id("buildcraftenergy", "oil_well_projection"),
            () -> OilWellProjectionProcessor.CODEC
         );
      }

      if (WATER_SPRING_BEDROCK == null) {
         WATER_SPRING_BEDROCK = Registry.register(
            BuiltInRegistries.STRUCTURE_PROCESSOR,
            BCRegistries.id("buildcraftenergy", "water_spring_bedrock"),
            () -> WaterSpringBedrockProcessor.CODEC
         );
      }
      //?}
   }
}

package buildcraft.energy.worldgen;

import buildcraft.energy.worldgen.integration.FineRichesTracker;
import buildcraft.energy.worldgen.integration.OilDesignBiomeNearbyTrigger;
import buildcraft.energy.worldgen.processor.BCEnergyStructureProcessorTypes;
import buildcraft.energy.worldgen.structure.BCEnergyStructures;
//? if >= 26.1.3 {
/*import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;*/
//?} else {
import net.minecraft.advancements.CriteriaTriggers;
//?}

/**
 * Runtime entry point for energy worldgen: structure types, processors, and advancement hooks.
 */
public final class BCEnergyWorldgen {
   //? if >= 26.1.3 {
   /*public static final OilDesignBiomeNearbyTrigger OIL_DESIGN_BIOME_NEARBY = Registry.register(
      BuiltInRegistries.TRIGGER_TYPES, "buildcraftenergy:oil_design_biome_nearby", new OilDesignBiomeNearbyTrigger()
   );*/
   //?} else {
   public static final OilDesignBiomeNearbyTrigger OIL_DESIGN_BIOME_NEARBY = CriteriaTriggers.register(
      "buildcraftenergy:oil_design_biome_nearby", new OilDesignBiomeNearbyTrigger()
   );
   //?}

   private BCEnergyWorldgen() {
   }

   public static void register() {
      BCEnergyStructures.registerStructureType();
      BCEnergyStructureProcessorTypes.register();
      FineRichesTracker.register();
   }
}

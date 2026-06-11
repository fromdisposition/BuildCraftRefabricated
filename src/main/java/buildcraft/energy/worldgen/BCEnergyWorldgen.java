package buildcraft.energy.worldgen;

import buildcraft.energy.worldgen.integration.FineRichesTracker;
import buildcraft.energy.worldgen.integration.OilDesignBiomeNearbyTrigger;
import buildcraft.energy.worldgen.processor.BCEnergyStructureProcessorTypes;
import buildcraft.energy.worldgen.structure.BCEnergyStructures;
import net.minecraft.advancements.CriteriaTriggers;

/**
 * Runtime entry point for energy worldgen: structure types, processors, and advancement hooks.
 */
public final class BCEnergyWorldgen {
   public static final OilDesignBiomeNearbyTrigger OIL_DESIGN_BIOME_NEARBY = CriteriaTriggers.register(
      "buildcraftenergy:oil_design_biome_nearby", new OilDesignBiomeNearbyTrigger()
   );

   private BCEnergyWorldgen() {
   }

   public static void register() {
      BCEnergyStructures.registerStructureType();
      BCEnergyStructureProcessorTypes.register();
      FineRichesTracker.register();
   }
}

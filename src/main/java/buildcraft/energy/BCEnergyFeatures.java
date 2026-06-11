package buildcraft.energy;

import buildcraft.energy.worldgen.adapter.FineRichesTracker;
import buildcraft.energy.worldgen.adapter.OilDesignBiomeNearbyTrigger;
import buildcraft.energy.worldgen.structure.BCEnergyStructures;
import net.minecraft.advancements.CriteriaTriggers;

/**
 * Registers oil worldgen structure type and advancement hooks. Deposit placement is handled by jigsaw structures
 * ({@link buildcraft.energy.worldgen.structure.OilDepositStructure}) with runtime checks in
 * {@link buildcraft.energy.worldgen.structure.OilStructureSpawnConditions}.
 */
public final class BCEnergyFeatures {
   public static final OilDesignBiomeNearbyTrigger OIL_DESIGN_BIOME_NEARBY = CriteriaTriggers.register(
      "buildcraftenergy:oil_design_biome_nearby", new OilDesignBiomeNearbyTrigger()
   );

   private BCEnergyFeatures() {
   }

   public static void register() {
      BCEnergyStructures.registerStructureType();
      buildcraft.energy.worldgen.structure.BCEnergyStructureProcessorTypes.register();
      FineRichesTracker.register();
   }
}

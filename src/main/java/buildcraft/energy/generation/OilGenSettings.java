package buildcraft.energy.generation;

import buildcraft.energy.BCEnergyConfig;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.resources.Identifier;

record OilGenSettings(
   Set<Identifier> richBiomes,
   Set<Identifier> extraRichBiomes,
   Set<Identifier> designBiomes,
   Set<Identifier> excludedBiomes,
   boolean biomeListIsBlacklist,
   double spawnChancePercentNormal,
   double spawnChancePercentRich,
   double spawnChancePercentOilPatch,
   double generationMultiplier,
   int typeWeightLarge,
   int typeWeightMedium,
   int typeWeightLake,
   boolean enableOilOnWater,
   boolean spawnOilSprings,
   boolean enableOilSpouts,
   int smallSpoutMinHeight,
   int smallSpoutMaxHeight,
   int largeSpoutMinHeight,
   int largeSpoutMaxHeight
) {
   private static volatile OilGenSettings INSTANCE;

   static OilGenSettings current() {
      OilGenSettings cached = INSTANCE;
      if (cached != null) {
         return cached;
      }
      synchronized (OilGenSettings.class) {
         cached = INSTANCE;
         if (cached == null) {
            INSTANCE = cached = capture();
         }
         return cached;
      }
   }

   static void invalidate() {
      INSTANCE = null;
   }

   boolean isBiomeExcluded(Identifier biomeId) {
      boolean inList = excludedBiomes.contains(biomeId);
      return biomeListIsBlacklist ? inList : !inList;
   }

   boolean isDesignBiome(Identifier biomeId) {
      return designBiomes.contains(biomeId);
   }

   private static OilGenSettings capture() {
      Set<Identifier> design = new HashSet<>(BCEnergyConfig.getForceExcessiveOilBiomes());
      design.add(OilBiomePatches.OIL_OCEAN);
      design.add(OilBiomePatches.OIL_DESERT);
      design.addAll(BCEnergyConfig.getRichSurfaceDepositBiomes());

      return new OilGenSettings(
         BCEnergyConfig.getRichSurfaceDepositBiomes(),
         BCEnergyConfig.getSurfaceDepositBiomes(),
         design,
         BCEnergyConfig.getExcludedBiomes(),
         BCEnergyConfig.biomeListMode.get() == BCEnergyConfig.ListMode.BLACKLIST,
         BCEnergyConfig.oilSpawnChancePercentNormal.get(),
         BCEnergyConfig.oilSpawnChancePercentRich.get(),
         BCEnergyConfig.oilSpawnChancePercentOilPatch.get(),
         BCEnergyConfig.oilGenerationMultiplier.get(),
         BCEnergyConfig.oilTypeWeightLarge.get(),
         BCEnergyConfig.oilTypeWeightMedium.get(),
         BCEnergyConfig.oilTypeWeightLake.get(),
         BCEnergyConfig.enableOilOnWater.get(),
         BCEnergyConfig.spawnOilSprings.get(),
         BCEnergyConfig.enableOilSpouts.get(),
         BCEnergyConfig.smallSpoutMinHeight.get(),
         BCEnergyConfig.smallSpoutMaxHeight.get(),
         BCEnergyConfig.largeSpoutMinHeight.get(),
         BCEnergyConfig.largeSpoutMaxHeight.get()
      );
   }
}

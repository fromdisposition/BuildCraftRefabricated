package buildcraft.energy.generation;

import buildcraft.energy.BCEnergyConfig;
import buildcraft.energy.BCEnergyWorldGen;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.resources.Identifier;

final class OilGenConfig {
   private static volatile OilGenConfig INSTANCE;

   final Set<Identifier> richBiomes;
   final Set<Identifier> extraRichBiomes;
   final Set<Identifier> designBiomes;
   final Set<Identifier> excludedBiomes;
   final boolean biomeListIsBlacklist;
   final double spawnChancePercentNormal;
   final double spawnChancePercentRich;
   final double spawnChancePercentOilPatch;
   final double generationMultiplier;
   final int typeWeightLarge;
   final int typeWeightMedium;
   final int typeWeightLake;
   final boolean enableOilOnWater;
   final boolean spawnOilSprings;
   final boolean enableOilSpouts;
   final int smallSpoutMinHeight;
   final int smallSpoutMaxHeight;
   final int largeSpoutMinHeight;
   final int largeSpoutMaxHeight;

   private OilGenConfig(
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
      this.richBiomes = richBiomes;
      this.extraRichBiomes = extraRichBiomes;
      this.designBiomes = designBiomes;
      this.excludedBiomes = excludedBiomes;
      this.biomeListIsBlacklist = biomeListIsBlacklist;
      this.spawnChancePercentNormal = spawnChancePercentNormal;
      this.spawnChancePercentRich = spawnChancePercentRich;
      this.spawnChancePercentOilPatch = spawnChancePercentOilPatch;
      this.generationMultiplier = generationMultiplier;
      this.typeWeightLarge = typeWeightLarge;
      this.typeWeightMedium = typeWeightMedium;
      this.typeWeightLake = typeWeightLake;
      this.enableOilOnWater = enableOilOnWater;
      this.spawnOilSprings = spawnOilSprings;
      this.enableOilSpouts = enableOilSpouts;
      this.smallSpoutMinHeight = smallSpoutMinHeight;
      this.smallSpoutMaxHeight = smallSpoutMaxHeight;
      this.largeSpoutMinHeight = largeSpoutMinHeight;
      this.largeSpoutMaxHeight = largeSpoutMaxHeight;
   }

   static OilGenConfig current() {
      OilGenConfig cached = INSTANCE;
      if (cached != null) {
         return cached;
      }
      synchronized (OilGenConfig.class) {
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

   private static OilGenConfig capture() {
      Set<Identifier> design = new HashSet<>(BCEnergyConfig.getForceExcessiveOilBiomes());
      design.add(BCEnergyWorldGen.OIL_OCEAN);
      design.add(BCEnergyWorldGen.OIL_DESERT);
      design.addAll(BCEnergyConfig.getRichSurfaceDepositBiomes());

      return new OilGenConfig(
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

   boolean isBiomeExcluded(Identifier biomeId) {
      boolean inList = this.excludedBiomes.contains(biomeId);
      return this.biomeListIsBlacklist ? inList : !inList;
   }

   boolean isDesignBiome(Identifier biomeId) {
      return this.designBiomes.contains(biomeId);
   }
}

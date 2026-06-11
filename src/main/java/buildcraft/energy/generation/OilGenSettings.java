package buildcraft.energy.generation.core;

import buildcraft.energy.generation.adapter.OilDepositFeatureConfiguration;

public record OilGenSettings(
   OilDepositFeatureConfiguration.ForcedTier forcedTier,
   int scanRadius,
   double spawnChancePercentNormal,
   double spawnChancePercentRich,
   double spawnChancePercentOilPatch,
   double generationMultiplier,
   int typeWeightLarge,
   int typeWeightMedium,
   int typeWeightLake,
   boolean enableOilSpouts,
   int smallSpoutMinHeight,
   int smallSpoutMaxHeight,
   int largeSpoutMinHeight,
   int largeSpoutMaxHeight,
   int lakeRadiusLarge,
   int lakeRadiusMedium,
   int lakeRadiusLake,
   int tendrilBaseLarge,
   int tendrilSpreadLarge,
   int tendrilBaseMedium,
   int tendrilSpreadMedium
) {
   public static OilGenSettings from(OilDepositFeatureConfiguration config) {
      return new OilGenSettings(
         config.forcedTier(),
         config.scanRadius(),
         config.spawnChancePercentNormal(),
         config.spawnChancePercentRich(),
         config.spawnChancePercentOilPatch(),
         config.generationMultiplier(),
         config.typeWeightLarge(),
         config.typeWeightMedium(),
         config.typeWeightLake(),
         config.enableOilSpouts(),
         config.spoutHeights().smallSpoutMinHeight(),
         config.spoutHeights().smallSpoutMaxHeight(),
         config.spoutHeights().largeSpoutMinHeight(),
         config.spoutHeights().largeSpoutMaxHeight(),
         config.geometryConfig().lakeRadiusLarge(),
         config.geometryConfig().lakeRadiusMedium(),
         config.geometryConfig().lakeRadiusLake(),
         config.geometryConfig().tendrilBaseLarge(),
         config.geometryConfig().tendrilSpreadLarge(),
         config.geometryConfig().tendrilBaseMedium(),
         config.geometryConfig().tendrilSpreadMedium()
      );
   }

   /**
    * Neighbour-origin scan gate: mirrors the tier's {@code rarity_filter} chance so stitched deposits do not appear
    * on every border. The decorating chunk itself skips this roll (already passed {@code rarity_filter}).
    */
   double neighborSpawnChanceFraction() {
      double percent = switch (this.forcedTier) {
         case NORMAL -> this.spawnChancePercentNormal;
         case RICH -> this.spawnChancePercentRich;
         case PATCH -> this.spawnChancePercentOilPatch;
      };
      percent *= this.generationMultiplier;
      if (percent <= 0.0) {
         return 0.0;
      }
      return Math.min(percent / 100.0, 1.0);
   }
}

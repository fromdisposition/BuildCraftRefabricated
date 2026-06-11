package buildcraft.energy.generation.core;

import buildcraft.energy.BCEnergyConfig;
import buildcraft.energy.generation.adapter.OilDepositFeatureConfiguration;
import buildcraft.lib.misc.RegistryKeyUtil;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

public record OilGenSettings(
   Set<Identifier> richBiomes,
   Set<Identifier> extraRichBiomes,
   Set<Identifier> designBiomes,
   Set<Identifier> excludedBiomes,
   boolean biomeListIsBlacklist,
   Set<Identifier> excludedDimensions,
   boolean dimensionListIsBlacklist,
   double spawnChancePercentNormal,
   double spawnChancePercentRich,
   double spawnChancePercentOilPatch,
   double generationMultiplier,
   OilDepositFeatureConfiguration.ForcedTier forcedTier,
   boolean useDatapackSpawnChance,
   int typeWeightLarge,
   int typeWeightMedium,
   int typeWeightLake,
   boolean enableOilOnWater,
   boolean enableOilOceanBiome,
   boolean enableOilDesertBiome,
   double oilOceanPatchChance,
   double oilDesertPatchChance,
   boolean spawnOilSprings,
   boolean enableOilSpouts,
   int smallSpoutMinHeight,
   int smallSpoutMaxHeight,
   int largeSpoutMinHeight,
   int largeSpoutMaxHeight,
   int scanRadius,
   int lakeRadiusLarge,
   int lakeRadiusMedium,
   int lakeRadiusLake,
   int tendrilBaseLarge,
   int tendrilSpreadLarge,
   int tendrilBaseMedium,
   int tendrilSpreadMedium
) {
   private static volatile OilGenSettings INSTANCE;

   public static OilGenSettings current() {
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

   public static void invalidate() {
      INSTANCE = null;
   }

   public boolean isBiomeExcluded(Holder<Biome> biome, Identifier biomeId) {
      boolean inList = excludedBiomes.contains(biomeId);
      if (biomeListIsBlacklist) {
         return inList || biome.is(BCEnergyBiomeTags.OIL_EXCLUDED_BIOME);
      }
      return !inList;
   }

   public boolean isDesignBiome(Holder<Biome> biome, Identifier biomeId) {
      return designBiomes.contains(biomeId) || biome.is(BCEnergyBiomeTags.OIL_DESIGN_BIOME);
   }

   public boolean isDimensionExcluded(ResourceKey<Level> dimension) {
      boolean inList = excludedDimensions.contains(RegistryKeyUtil.id(dimension));
      return dimensionListIsBlacklist ? inList : !inList;
   }

   public static OilGenSettings merged(OilDepositFeatureConfiguration featureConfig) {
      OilGenSettings runtime = current();
      return new OilGenSettings(
         runtime.richBiomes(),
         runtime.extraRichBiomes(),
         runtime.designBiomes(),
         runtime.excludedBiomes(),
         runtime.biomeListIsBlacklist(),
         runtime.excludedDimensions(),
         runtime.dimensionListIsBlacklist(),
         runtime.spawnChancePercentNormal(),
         runtime.spawnChancePercentRich(),
         runtime.spawnChancePercentOilPatch(),
         runtime.generationMultiplier(),
         featureConfig.forcedTier(),
         featureConfig.useDatapackSpawnChance(),
         runtime.typeWeightLarge(),
         runtime.typeWeightMedium(),
         runtime.typeWeightLake(),
         runtime.enableOilOnWater(),
         runtime.enableOilOceanBiome(),
         runtime.enableOilDesertBiome(),
         runtime.oilOceanPatchChance(),
         runtime.oilDesertPatchChance(),
         runtime.spawnOilSprings(),
         runtime.enableOilSpouts(),
         runtime.smallSpoutMinHeight(),
         runtime.smallSpoutMaxHeight(),
         runtime.largeSpoutMinHeight(),
         runtime.largeSpoutMaxHeight(),
         featureConfig.scanRadius(),
         featureConfig.geometryConfig().lakeRadiusLarge(),
         featureConfig.geometryConfig().lakeRadiusMedium(),
         featureConfig.geometryConfig().lakeRadiusLake(),
         featureConfig.geometryConfig().tendrilBaseLarge(),
         featureConfig.geometryConfig().tendrilSpreadLarge(),
         featureConfig.geometryConfig().tendrilBaseMedium(),
         featureConfig.geometryConfig().tendrilSpreadMedium()
      );
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
         BCEnergyConfig.getExcludedDimensions(),
         BCEnergyConfig.dimensionListMode.get() == BCEnergyConfig.ListMode.BLACKLIST,
         BCEnergyConfig.oilSpawnChancePercentNormal.get(),
         BCEnergyConfig.oilSpawnChancePercentRich.get(),
         BCEnergyConfig.oilSpawnChancePercentOilPatch.get(),
         BCEnergyConfig.oilGenerationMultiplier.get(),
         OilDepositFeatureConfiguration.ForcedTier.AUTO,
         false,
         BCEnergyConfig.oilTypeWeightLarge.get(),
         BCEnergyConfig.oilTypeWeightMedium.get(),
         BCEnergyConfig.oilTypeWeightLake.get(),
         BCEnergyConfig.enableOilOnWater.get(),
         BCEnergyConfig.enableOilOceanBiome.get(),
         BCEnergyConfig.enableOilDesertBiome.get(),
         BCEnergyConfig.oilOceanPatchChance.get(),
         BCEnergyConfig.oilDesertPatchChance.get(),
         BCEnergyConfig.spawnOilSprings.get(),
         BCEnergyConfig.enableOilSpouts.get(),
         BCEnergyConfig.smallSpoutMinHeight.get(),
         BCEnergyConfig.smallSpoutMaxHeight.get(),
         BCEnergyConfig.largeSpoutMinHeight.get(),
         BCEnergyConfig.largeSpoutMaxHeight.get(),
         OilDepositFeatureConfiguration.DEFAULT.scanRadius(),
         OilDepositFeatureConfiguration.DEFAULT.geometryConfig().lakeRadiusLarge(),
         OilDepositFeatureConfiguration.DEFAULT.geometryConfig().lakeRadiusMedium(),
         OilDepositFeatureConfiguration.DEFAULT.geometryConfig().lakeRadiusLake(),
         OilDepositFeatureConfiguration.DEFAULT.geometryConfig().tendrilBaseLarge(),
         OilDepositFeatureConfiguration.DEFAULT.geometryConfig().tendrilSpreadLarge(),
         OilDepositFeatureConfiguration.DEFAULT.geometryConfig().tendrilBaseMedium(),
         OilDepositFeatureConfiguration.DEFAULT.geometryConfig().tendrilSpreadMedium()
      );
   }
}

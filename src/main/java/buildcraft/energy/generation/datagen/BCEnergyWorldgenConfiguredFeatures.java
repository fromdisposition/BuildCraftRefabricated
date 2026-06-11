package buildcraft.energy.generation.datagen;

import buildcraft.energy.BCEnergyFeatures;
import buildcraft.energy.generation.adapter.OilDepositFeatureConfiguration;
import buildcraft.fabric.BCRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;

final class BCEnergyWorldgenConfiguredFeatures {
   static final ResourceKey<ConfiguredFeature<?, ?>> OIL_DEPOSIT_NORMAL = ResourceKey.create(
      Registries.CONFIGURED_FEATURE, BCRegistries.id("buildcraftenergy", "oil_deposit_normal")
   );
   static final ResourceKey<ConfiguredFeature<?, ?>> OIL_DEPOSIT_RICH = ResourceKey.create(
      Registries.CONFIGURED_FEATURE, BCRegistries.id("buildcraftenergy", "oil_deposit_rich")
   );
   static final ResourceKey<ConfiguredFeature<?, ?>> OIL_DEPOSIT_PATCH = ResourceKey.create(
      Registries.CONFIGURED_FEATURE, BCRegistries.id("buildcraftenergy", "oil_deposit_patch")
   );

   private BCEnergyWorldgenConfiguredFeatures() {
   }

   static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
      @SuppressWarnings("unchecked")
      Feature<OilDepositFeatureConfiguration> oilDeposit = (Feature<OilDepositFeatureConfiguration>) BCEnergyFeatures.OIL_DEPOSIT;
      context.register(
         OIL_DEPOSIT_NORMAL,
         new ConfiguredFeature<>(oilDeposit, withForcedTier(OilDepositFeatureConfiguration.ForcedTier.NORMAL, true))
      );
      context.register(
         OIL_DEPOSIT_RICH,
         new ConfiguredFeature<>(oilDeposit, withForcedTier(OilDepositFeatureConfiguration.ForcedTier.RICH, true))
      );
      context.register(
         OIL_DEPOSIT_PATCH,
         new ConfiguredFeature<>(oilDeposit, withForcedTier(OilDepositFeatureConfiguration.ForcedTier.PATCH, true))
      );
   }

   private static OilDepositFeatureConfiguration withForcedTier(
      OilDepositFeatureConfiguration.ForcedTier forcedTier, boolean useDatapackSpawnChance
   ) {
      return new OilDepositFeatureConfiguration(
         OilDepositFeatureConfiguration.DEFAULT.scanRadius(),
         OilDepositFeatureConfiguration.DEFAULT.spawnChancePercentNormal(),
         OilDepositFeatureConfiguration.DEFAULT.spawnChancePercentRich(),
         OilDepositFeatureConfiguration.DEFAULT.spawnChancePercentOilPatch(),
         OilDepositFeatureConfiguration.DEFAULT.generationMultiplier(),
         forcedTier,
         useDatapackSpawnChance,
         OilDepositFeatureConfiguration.DEFAULT.typeWeightLarge(),
         OilDepositFeatureConfiguration.DEFAULT.typeWeightMedium(),
         OilDepositFeatureConfiguration.DEFAULT.typeWeightLake(),
         OilDepositFeatureConfiguration.DEFAULT.patchConfig(),
         OilDepositFeatureConfiguration.DEFAULT.spawnOilSprings(),
         OilDepositFeatureConfiguration.DEFAULT.enableOilSpouts(),
         OilDepositFeatureConfiguration.DEFAULT.spoutHeights(),
         OilDepositFeatureConfiguration.DEFAULT.geometryConfig()
      );
   }
}

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
         new ConfiguredFeature<>(oilDeposit, withForcedTier(OilDepositFeatureConfiguration.ForcedTier.NORMAL))
      );
      context.register(
         OIL_DEPOSIT_RICH,
         new ConfiguredFeature<>(oilDeposit, withForcedTier(OilDepositFeatureConfiguration.ForcedTier.RICH))
      );
      context.register(
         OIL_DEPOSIT_PATCH,
         new ConfiguredFeature<>(oilDeposit, withForcedTier(OilDepositFeatureConfiguration.ForcedTier.PATCH))
      );
   }

   private static OilDepositFeatureConfiguration withForcedTier(OilDepositFeatureConfiguration.ForcedTier forcedTier) {
      OilDepositFeatureConfiguration defaults = OilDepositFeatureConfiguration.DEFAULT;
      return new OilDepositFeatureConfiguration(
         defaults.scanRadius(),
         defaults.spawnChancePercentNormal(),
         defaults.spawnChancePercentRich(),
         defaults.spawnChancePercentOilPatch(),
         defaults.generationMultiplier(),
         forcedTier,
         defaults.typeWeightLarge(),
         defaults.typeWeightMedium(),
         defaults.typeWeightLake(),
         defaults.spawnOilSprings(),
         defaults.enableOilSpouts(),
         defaults.spoutHeights(),
         defaults.geometryConfig()
      );
   }
}

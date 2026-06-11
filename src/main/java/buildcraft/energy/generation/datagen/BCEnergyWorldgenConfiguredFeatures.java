package buildcraft.energy.generation.datagen;

import buildcraft.energy.BCEnergyFeatures;
import buildcraft.fabric.BCRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

final class BCEnergyWorldgenConfiguredFeatures {
   static final ResourceKey<ConfiguredFeature<?, ?>> OIL_DEPOSIT = ResourceKey.create(
      Registries.CONFIGURED_FEATURE, BCRegistries.id("buildcraftenergy", "oil_deposit")
   );

   private BCEnergyWorldgenConfiguredFeatures() {
   }

   static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
      @SuppressWarnings("unchecked")
      Feature<NoneFeatureConfiguration> oilDeposit = (Feature<NoneFeatureConfiguration>) BCEnergyFeatures.OIL_DEPOSIT;
      context.register(OIL_DEPOSIT, new ConfiguredFeature<>(oilDeposit, NoneFeatureConfiguration.INSTANCE));
   }
}

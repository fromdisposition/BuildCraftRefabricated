package buildcraft.energy.generation.datagen;

import buildcraft.energy.BCEnergyFeatures;
import buildcraft.energy.generation.adapter.OilDepositFeatureConfiguration;
import java.util.List;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.RarityFilter;

final class BCEnergyWorldgenPlacedFeatures {
   private BCEnergyWorldgenPlacedFeatures() {
   }

   static void bootstrap(BootstrapContext<PlacedFeature> context) {
      HolderGetter<ConfiguredFeature<?, ?>> configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);
      context.register(
         BCEnergyFeatures.OIL_DEPOSIT_NORMAL_PLACED,
         new PlacedFeature(
            configuredFeatures.getOrThrow(BCEnergyWorldgenConfiguredFeatures.OIL_DEPOSIT_NORMAL),
            List.of(CountPlacement.of(1), RarityFilter.onAverageOnceEvery(chanceToRarity(0.15, 1.0)), BiomeFilter.biome())
         )
      );
      context.register(
         BCEnergyFeatures.OIL_DEPOSIT_RICH_PLACED,
         new PlacedFeature(
            configuredFeatures.getOrThrow(BCEnergyWorldgenConfiguredFeatures.OIL_DEPOSIT_RICH),
            List.of(CountPlacement.of(1), RarityFilter.onAverageOnceEvery(chanceToRarity(0.3, 1.0)), BiomeFilter.biome())
         )
      );
      context.register(
         BCEnergyFeatures.OIL_DEPOSIT_PLACED,
         new PlacedFeature(
            configuredFeatures.getOrThrow(BCEnergyWorldgenConfiguredFeatures.OIL_DEPOSIT_PATCH),
            List.of(
               CountPlacement.of(1),
               RarityFilter.onAverageOnceEvery(chanceToRarity(OilDepositFeatureConfiguration.DEFAULT.spawnChancePercentOilPatch(), 1.0)),
               BiomeFilter.biome()
            )
         )
      );
   }

   private static int chanceToRarity(double percent, double multiplier) {
      double chanceFraction = Math.min(Math.max((percent * multiplier) / 100.0, 0.0), 1.0);
      if (chanceFraction <= 0.0) {
         return Integer.MAX_VALUE;
      }
      return Math.max(1, (int) Math.round(1.0 / chanceFraction));
   }
}

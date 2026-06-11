package buildcraft.energy.generation.datagen;

import buildcraft.energy.BCEnergyFeatures;
import java.util.List;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

final class BCEnergyWorldgenPlacedFeatures {
   private BCEnergyWorldgenPlacedFeatures() {
   }

   static void bootstrap(BootstrapContext<PlacedFeature> context) {
      HolderGetter<ConfiguredFeature<?, ?>> configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);
      context.register(
         BCEnergyFeatures.OIL_DEPOSIT_PLACED,
         new PlacedFeature(
            configuredFeatures.getOrThrow(BCEnergyWorldgenConfiguredFeatures.OIL_DEPOSIT),
            List.of(CountPlacement.of(1), BiomeFilter.biome())
         )
      );
   }
}

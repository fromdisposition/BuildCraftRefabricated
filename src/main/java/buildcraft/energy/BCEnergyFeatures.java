package buildcraft.energy;

import buildcraft.core.BCCoreConfig;
import buildcraft.energy.generation.FineRichesTracker;
import buildcraft.energy.generation.OilDepositFeature;
import buildcraft.fabric.BCRegistries;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public final class BCEnergyFeatures {
   public static final ResourceKey<PlacedFeature> OIL_DEPOSIT_PLACED = ResourceKey.create(
      Registries.PLACED_FEATURE, BCRegistries.id("buildcraftenergy", "oil_deposit")
   );
   public static Feature<NoneFeatureConfiguration> OIL_DEPOSIT;

   private BCEnergyFeatures() {
   }

   public static void register() {
      OIL_DEPOSIT = (Feature<NoneFeatureConfiguration>) Registry.register(
         BuiltInRegistries.FEATURE, BCRegistries.id("buildcraftenergy", "oil_deposit"), new OilDepositFeature(NoneFeatureConfiguration.CODEC)
      );

      if (BCCoreConfig.worldGen.get() && BCEnergyConfig.enableOilGeneration.get()) {
         BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), Decoration.UNDERGROUND_DECORATION, OIL_DEPOSIT_PLACED);
         FineRichesTracker.register();
      }
   }
}

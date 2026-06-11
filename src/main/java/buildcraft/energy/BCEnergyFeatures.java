package buildcraft.energy;

import buildcraft.energy.generation.FineRichesTracker;
import buildcraft.energy.generation.OilDepositFeature;
import buildcraft.fabric.BCRegistries;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/**
 * Registers the oil deposit feature type and adds it to overworld biomes via Fabric biome modifications.
 *
 * <p>Runtime enable flags ({@code core.worldGen}, {@code energy.generator.enableOilGeneration}) are enforced in
 * {@link buildcraft.energy.generation.OilDepositFeature} so config reload does not require re-registering biomes.
 */
public final class BCEnergyFeatures {
   public static final ResourceKey<PlacedFeature> OIL_DEPOSIT_PLACED = ResourceKey.create(
      Registries.PLACED_FEATURE, BCRegistries.id("buildcraftenergy", "oil_deposit")
   );
   public static Feature<NoneFeatureConfiguration> OIL_DEPOSIT;

   private BCEnergyFeatures() {
   }

   public static void registerFeatureType() {
      if (OIL_DEPOSIT == null) {
         OIL_DEPOSIT = (Feature<NoneFeatureConfiguration>) Registry.register(
            BuiltInRegistries.FEATURE, BCRegistries.id("buildcraftenergy", "oil_deposit"), new OilDepositFeature(NoneFeatureConfiguration.CODEC)
         );
      }
   }

   public static void register() {
      registerFeatureType();

      BiomeModifications.create(BCRegistries.id("buildcraftenergy", "oil_deposit"))
         .add(
            ModificationPhase.ADDITIONS,
            BiomeSelectors.foundInOverworld(),
            context -> context.getGenerationSettings().addFeature(Decoration.UNDERGROUND_DECORATION, OIL_DEPOSIT_PLACED)
         );

      FineRichesTracker.register();
   }
}

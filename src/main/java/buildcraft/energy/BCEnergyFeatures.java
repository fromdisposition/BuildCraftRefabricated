package buildcraft.energy;

import buildcraft.energy.generation.adapter.FineRichesTracker;
import buildcraft.energy.generation.adapter.OilDesignBiomeNearbyTrigger;
import buildcraft.energy.generation.adapter.OilDepositFeature;
import buildcraft.energy.generation.adapter.OilDepositFeatureConfiguration;
import buildcraft.energy.generation.core.BCEnergyBiomeTags;
import buildcraft.fabric.BCRegistries;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/**
 * Registers the oil deposit feature type and adds it to overworld biomes via Fabric biome modifications.
 *
 * <p>Runtime enable flags ({@code core.worldGen}, {@code energy.generator.enableOilGeneration}) are enforced in
 * {@link buildcraft.energy.generation.adapter.OilDepositFeature} so config reload does not require re-registering biomes.
 */
public final class BCEnergyFeatures {
   public static final OilDesignBiomeNearbyTrigger OIL_DESIGN_BIOME_NEARBY = CriteriaTriggers.register(
      "buildcraftenergy:oil_design_biome_nearby", new OilDesignBiomeNearbyTrigger()
   );
   public static final ResourceKey<PlacedFeature> OIL_DEPOSIT_NORMAL_PLACED = ResourceKey.create(
      Registries.PLACED_FEATURE, BCRegistries.id("buildcraftenergy", "oil_deposit_normal")
   );
   public static final ResourceKey<PlacedFeature> OIL_DEPOSIT_RICH_PLACED = ResourceKey.create(
      Registries.PLACED_FEATURE, BCRegistries.id("buildcraftenergy", "oil_deposit_rich")
   );
   public static final ResourceKey<PlacedFeature> OIL_DEPOSIT_PLACED = ResourceKey.create(
      Registries.PLACED_FEATURE, BCRegistries.id("buildcraftenergy", "oil_deposit_patch")
   );
   public static Feature<OilDepositFeatureConfiguration> OIL_DEPOSIT;

   private BCEnergyFeatures() {
   }

   public static void registerFeatureType() {
      if (OIL_DEPOSIT == null) {
         OIL_DEPOSIT = (Feature<OilDepositFeatureConfiguration>) Registry.register(
            BuiltInRegistries.FEATURE, BCRegistries.id("buildcraftenergy", "oil_deposit"), new OilDepositFeature(OilDepositFeatureConfiguration.CODEC)
         );
      }
   }

   public static void register() {
      registerFeatureType();

      BiomeModifications.create(BCRegistries.id("buildcraftenergy", "oil_deposit_normal"))
         .add(
            ModificationPhase.ADDITIONS,
            BiomeSelectors.foundInOverworld().and(BiomeSelectors.tag(BCEnergyBiomeTags.OIL_RICH_BIOME).negate()),
            context -> context.getGenerationSettings().addFeature(Decoration.UNDERGROUND_DECORATION, OIL_DEPOSIT_NORMAL_PLACED)
         );

      BiomeModifications.create(BCRegistries.id("buildcraftenergy", "oil_deposit_rich"))
         .add(
            ModificationPhase.ADDITIONS,
            BiomeSelectors.foundInOverworld().and(BiomeSelectors.tag(BCEnergyBiomeTags.OIL_RICH_BIOME)),
            context -> context.getGenerationSettings().addFeature(Decoration.UNDERGROUND_DECORATION, OIL_DEPOSIT_RICH_PLACED)
         );

      BiomeModifications.create(BCRegistries.id("buildcraftenergy", "oil_deposit_patch"))
         .add(
            ModificationPhase.ADDITIONS,
            BiomeSelectors.foundInOverworld(),
            context -> context.getGenerationSettings().addFeature(Decoration.UNDERGROUND_DECORATION, OIL_DEPOSIT_PLACED)
         );

      FineRichesTracker.register();
   }
}

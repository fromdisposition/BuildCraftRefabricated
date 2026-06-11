package buildcraft.energy.generation.adapter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record OilDepositFeatureConfiguration(
   int scanRadius,
   double spawnChancePercentNormal,
   double spawnChancePercentRich,
   double spawnChancePercentOilPatch,
   double generationMultiplier,
   ForcedTier forcedTier,
   boolean useDatapackSpawnChance,
   int typeWeightLarge,
   int typeWeightMedium,
   int typeWeightLake,
   PatchConfig patchConfig,
   boolean spawnOilSprings,
   boolean enableOilSpouts,
   SpoutHeights spoutHeights,
   GeometryConfig geometryConfig
) implements FeatureConfiguration {
   public static final OilDepositFeatureConfiguration DEFAULT = new OilDepositFeatureConfiguration(
      5,
      0.15,
      0.3,
      2.0,
      1.0,
      ForcedTier.AUTO,
      false,
      20,
      60,
      20,
      new PatchConfig(true, true, true, 0.025, 0.08),
      true,
      true,
      new SpoutHeights(6, 12, 10, 20),
      new GeometryConfig(4, 2, 6, 25, 20, 5, 10)
   );

   public static final Codec<OilDepositFeatureConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
      Codec.intRange(1, 32).fieldOf("scan_radius").forGetter(OilDepositFeatureConfiguration::scanRadius),
      Codec.doubleRange(0.0, 100.0).fieldOf("spawn_chance_percent_normal").forGetter(OilDepositFeatureConfiguration::spawnChancePercentNormal),
      Codec.doubleRange(0.0, 100.0).fieldOf("spawn_chance_percent_rich").forGetter(OilDepositFeatureConfiguration::spawnChancePercentRich),
      Codec.doubleRange(0.0, 100.0).fieldOf("spawn_chance_percent_oil_patch").forGetter(OilDepositFeatureConfiguration::spawnChancePercentOilPatch),
      Codec.doubleRange(0.0, 64.0).fieldOf("generation_multiplier").forGetter(OilDepositFeatureConfiguration::generationMultiplier),
      ForcedTier.CODEC.fieldOf("forced_tier").forGetter(OilDepositFeatureConfiguration::forcedTier),
      Codec.BOOL.fieldOf("use_datapack_spawn_chance").forGetter(OilDepositFeatureConfiguration::useDatapackSpawnChance),
      Codec.INT.fieldOf("type_weight_large").forGetter(OilDepositFeatureConfiguration::typeWeightLarge),
      Codec.INT.fieldOf("type_weight_medium").forGetter(OilDepositFeatureConfiguration::typeWeightMedium),
      Codec.INT.fieldOf("type_weight_lake").forGetter(OilDepositFeatureConfiguration::typeWeightLake),
      PatchConfig.CODEC.fieldOf("patch_config").forGetter(OilDepositFeatureConfiguration::patchConfig),
      Codec.BOOL.fieldOf("spawn_oil_springs").forGetter(OilDepositFeatureConfiguration::spawnOilSprings),
      Codec.BOOL.fieldOf("enable_oil_spouts").forGetter(OilDepositFeatureConfiguration::enableOilSpouts),
      SpoutHeights.CODEC.fieldOf("spout_heights").forGetter(OilDepositFeatureConfiguration::spoutHeights),
      GeometryConfig.CODEC.fieldOf("geometry").forGetter(OilDepositFeatureConfiguration::geometryConfig)
   ).apply(instance, OilDepositFeatureConfiguration::new));

   public enum ForcedTier {
      AUTO,
      NORMAL,
      RICH,
      PATCH;

      static final Codec<ForcedTier> CODEC = Codec.STRING.xmap(
         value -> ForcedTier.valueOf(value.toUpperCase()),
         value -> value.name().toLowerCase()
      );
   }

   public record PatchConfig(
      boolean enableOilOnWater,
      boolean enableOilOceanBiome,
      boolean enableOilDesertBiome,
      double oilOceanPatchChance,
      double oilDesertPatchChance
   ) {
      static final Codec<PatchConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
         Codec.BOOL.fieldOf("enable_oil_on_water").forGetter(PatchConfig::enableOilOnWater),
         Codec.BOOL.fieldOf("enable_oil_ocean_biome").forGetter(PatchConfig::enableOilOceanBiome),
         Codec.BOOL.fieldOf("enable_oil_desert_biome").forGetter(PatchConfig::enableOilDesertBiome),
         Codec.doubleRange(0.0, 1.0).fieldOf("oil_ocean_patch_chance").forGetter(PatchConfig::oilOceanPatchChance),
         Codec.doubleRange(0.0, 1.0).fieldOf("oil_desert_patch_chance").forGetter(PatchConfig::oilDesertPatchChance)
      ).apply(instance, PatchConfig::new));
   }

   public record SpoutHeights(
      int smallSpoutMinHeight,
      int smallSpoutMaxHeight,
      int largeSpoutMinHeight,
      int largeSpoutMaxHeight
   ) {
      static final Codec<SpoutHeights> CODEC = RecordCodecBuilder.create(instance -> instance.group(
         Codec.INT.fieldOf("small_spout_min_height").forGetter(SpoutHeights::smallSpoutMinHeight),
         Codec.INT.fieldOf("small_spout_max_height").forGetter(SpoutHeights::smallSpoutMaxHeight),
         Codec.INT.fieldOf("large_spout_min_height").forGetter(SpoutHeights::largeSpoutMinHeight),
         Codec.INT.fieldOf("large_spout_max_height").forGetter(SpoutHeights::largeSpoutMaxHeight)
      ).apply(instance, SpoutHeights::new));
   }

   public record GeometryConfig(
      int lakeRadiusLarge,
      int lakeRadiusMedium,
      int lakeRadiusLake,
      int tendrilBaseLarge,
      int tendrilSpreadLarge,
      int tendrilBaseMedium,
      int tendrilSpreadMedium
   ) {
      static final Codec<GeometryConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
         Codec.intRange(1, 32).fieldOf("lake_radius_large").forGetter(GeometryConfig::lakeRadiusLarge),
         Codec.intRange(1, 32).fieldOf("lake_radius_medium").forGetter(GeometryConfig::lakeRadiusMedium),
         Codec.intRange(1, 32).fieldOf("lake_radius_lake").forGetter(GeometryConfig::lakeRadiusLake),
         Codec.intRange(1, 128).fieldOf("tendril_base_large").forGetter(GeometryConfig::tendrilBaseLarge),
         Codec.intRange(0, 128).fieldOf("tendril_spread_large").forGetter(GeometryConfig::tendrilSpreadLarge),
         Codec.intRange(1, 128).fieldOf("tendril_base_medium").forGetter(GeometryConfig::tendrilBaseMedium),
         Codec.intRange(0, 128).fieldOf("tendril_spread_medium").forGetter(GeometryConfig::tendrilSpreadMedium)
      ).apply(instance, GeometryConfig::new));
   }
}

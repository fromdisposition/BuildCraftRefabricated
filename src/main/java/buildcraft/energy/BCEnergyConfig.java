/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy;

import buildcraft.core.BCCoreConfig;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.resources.Identifier;

public class BCEnergyConfig {
   public static final BCCoreConfig.BooleanValue oilIsSticky = new BCCoreConfig.BooleanValue(false);
   public static final BCCoreConfig.BooleanValue enableRfEngine = new BCCoreConfig.BooleanValue(false);
   public static final BCCoreConfig.BooleanValue enableMjDynamo = new BCCoreConfig.BooleanValue(false);
   /** Master switch: oil in ocean biomes and on water (default on). */
   public static final BCCoreConfig.BooleanValue enableOilOnWater = new BCCoreConfig.BooleanValue(true);
   /** Synthetic oil_ocean patches inside shallow ocean (only if {@link #enableOilOnWater} is on). */
   public static final BCCoreConfig.BooleanValue enableOilOceanBiome = new BCCoreConfig.BooleanValue(true);
   public static final BCCoreConfig.BooleanValue enableOilDesertBiome = new BCCoreConfig.BooleanValue(true);
   /** Shallow-ocean columns that become BC oil_ocean patches (percent of shallow ocean area). */
   public static final BCCoreConfig.DoubleValue oilOceanPatchChance = new BCCoreConfig.DoubleValue(0.025);
   /** Desert/badlands columns that become BC oil_desert patches (percent of desert area). */
   public static final BCCoreConfig.DoubleValue oilDesertPatchChance = new BCCoreConfig.DoubleValue(0.08);
   /** Per-chunk chance (%) — default biomes including ocean. */
   public static final BCCoreConfig.DoubleValue oilSpawnChancePercentNormal = new BCCoreConfig.DoubleValue(0.12);
   /** Per-chunk chance (%) — desert, badlands, {@link #richSurfaceDepositBiomes} (small bonus over normal). */
   public static final BCCoreConfig.DoubleValue oilSpawnChancePercentRich = new BCCoreConfig.DoubleValue(0.25);
   /** Per-chunk chance (%) — synthetic oil_ocean / oil_desert patches. */
   public static final BCCoreConfig.DoubleValue oilSpawnChancePercentOilPatch = new BCCoreConfig.DoubleValue(2.0);
   /** Relative weight when oil spawns: large underground reservoir. */
   public static final BCCoreConfig.IntValue oilTypeWeightLarge = new BCCoreConfig.IntValue(20);
   /** Relative weight when oil spawns: medium underground reservoir. */
   public static final BCCoreConfig.IntValue oilTypeWeightMedium = new BCCoreConfig.IntValue(60);
   /** Relative weight when oil spawns: surface lake (ocean, rich, oil-patch tiers). */
   public static final BCCoreConfig.IntValue oilTypeWeightLake = new BCCoreConfig.IntValue(20);
   /** Global multiplier for all three spawn chance percents (1.0 = default). */
   public static final BCCoreConfig.DoubleValue oilGenerationMultiplier = new BCCoreConfig.DoubleValue(1.0);
   public static final BCCoreConfig.BooleanValue enableOilBurn = new BCCoreConfig.BooleanValue(true);
   public static final BCCoreConfig.BooleanValue useRfNaming = new BCCoreConfig.BooleanValue(false);
   public static final BCCoreConfig.BooleanValue useFullUnitNames = new BCCoreConfig.BooleanValue(true);
   public static final BCCoreConfig.BooleanValue enableOilGeneration = new BCCoreConfig.BooleanValue(true);
   /** @deprecated Use {@link #oilGenerationMultiplier}. */
   @Deprecated
   public static final BCCoreConfig.DoubleValue oilWellGenerationRate = new BCCoreConfig.DoubleValue(1.0);
   public static final BCCoreConfig.BooleanValue enableOilSpouts = new BCCoreConfig.BooleanValue(true);
   public static final BCCoreConfig.BooleanValue spawnOilSprings = new BCCoreConfig.BooleanValue(true);
   public static final BCCoreConfig.IntValue smallSpoutMinHeight = new BCCoreConfig.IntValue(6);
   public static final BCCoreConfig.IntValue smallSpoutMaxHeight = new BCCoreConfig.IntValue(12);
   public static final BCCoreConfig.IntValue finiteSpoutMinHeight = smallSpoutMinHeight;
   public static final BCCoreConfig.IntValue finiteSpoutMaxHeight = smallSpoutMaxHeight;
   public static final BCCoreConfig.IntValue largeSpoutMinHeight = new BCCoreConfig.IntValue(10);
   public static final BCCoreConfig.IntValue largeSpoutMaxHeight = new BCCoreConfig.IntValue(20);
   /** @deprecated Legacy BC keys — migrated to {@link #oilSpawnChancePercentNormal} etc. on load. */
   @Deprecated
   public static final BCCoreConfig.DoubleValue mediumOilGenProb = new BCCoreConfig.DoubleValue(0.001);
   /** @deprecated Legacy BC keys — migrated on load. */
   @Deprecated
   public static final BCCoreConfig.DoubleValue largeOilGenProb = new BCCoreConfig.DoubleValue(4.0E-4);
   /** @deprecated Legacy BC keys — migrated on load. */
   @Deprecated
   public static final BCCoreConfig.DoubleValue smallOilGenProb = new BCCoreConfig.DoubleValue(0.02);
   /** Biomes flagged as oil-themed for advancements / markers (not used for spawn math). */
   public static final BCCoreConfig.StringListValue forceExcessiveOilBiomes = new BCCoreConfig.StringListValue(
      List.of("buildcraftenergy:oil_desert", "buildcraftenergy:oil_ocean")
   );
   /** Biomes flagged for oil-themed world design (advancements); BC 8.0 spawn rolls do not special-case these. */
   public static final BCCoreConfig.StringListValue richSurfaceDepositBiomes = new BCCoreConfig.StringListValue(
      List.of("minecraft:desert", "minecraft:badlands", "minecraft:wooded_badlands")
   );
   /** Extra biomes treated as {@code RICH} tier (in addition to {@link #richSurfaceDepositBiomes}). */
   public static final BCCoreConfig.StringListValue surfaceDepositBiomes = new BCCoreConfig.StringListValue(List.of());
   public static final BCCoreConfig.StringListValue standardSurfaceDepositBiomes = new BCCoreConfig.StringListValue(
      List.of(
         "minecraft:jungle", "minecraft:sparse_jungle", "minecraft:bamboo_jungle", "minecraft:ice_spikes", "minecraft:snowy_beach", "minecraft:frozen_river"
      )
   );
   public static final BCCoreConfig.StringListValue mountainousSurfaceDepositBiomes = new BCCoreConfig.StringListValue(
      List.of(
         "minecraft:windswept_hills",
         "minecraft:windswept_gravelly_hills",
         "minecraft:windswept_forest",
         "minecraft:jagged_peaks",
         "minecraft:frozen_peaks",
         "minecraft:stony_peaks",
         "minecraft:snowy_slopes",
         "minecraft:meadow",
         "minecraft:grove",
         "minecraft:cherry_grove"
      )
   );
   public static final BCCoreConfig.StringListValue excludedBiomes = new BCCoreConfig.StringListValue(List.of("minecraft:the_void", "minecraft:river"));
   public static final BCCoreConfig.EnumValue<BCEnergyConfig.ListMode> biomeListMode = new BCCoreConfig.EnumValue<>(BCEnergyConfig.ListMode.BLACKLIST);
   public static final BCCoreConfig.StringListValue excludedDimensions = new BCCoreConfig.StringListValue(List.of("minecraft:the_nether", "minecraft:the_end"));
   public static final BCCoreConfig.EnumValue<BCEnergyConfig.ListMode> dimensionListMode = new BCCoreConfig.EnumValue<>(BCEnergyConfig.ListMode.BLACKLIST);

   public static void buildWorldgen(Object builder) {
   }

   public static void buildGeneral(Object builder) {
   }

   public static void buildDisplay(Object builder) {
   }

   public static String rfFeKey(String baseKey) {
      return baseKey;
   }

   public static Set<Identifier> getForceExcessiveOilBiomes() {
      return forceExcessiveOilBiomes.get().stream().<Identifier>map(Identifier::parse).collect(Collectors.toCollection(HashSet::new));
   }

   public static Set<Identifier> getSurfaceDepositBiomes() {
      return surfaceDepositBiomes.get().stream().<Identifier>map(Identifier::parse).collect(Collectors.toCollection(HashSet::new));
   }

   public static Set<Identifier> getRichSurfaceDepositBiomes() {
      return richSurfaceDepositBiomes.get().stream().<Identifier>map(Identifier::parse).collect(Collectors.toSet());
   }

   public static Set<Identifier> getStandardSurfaceDepositBiomes() {
      return standardSurfaceDepositBiomes.get().stream().<Identifier>map(Identifier::parse).collect(Collectors.toSet());
   }

   public static Set<Identifier> getMountainousSurfaceDepositBiomes() {
      return mountainousSurfaceDepositBiomes.get().stream().<Identifier>map(Identifier::parse).collect(Collectors.toSet());
   }

   public static Set<Identifier> getExcludedBiomes() {
      return excludedBiomes.get().stream().<Identifier>map(Identifier::parse).collect(Collectors.toSet());
   }

   public static Set<Identifier> getExcludedDimensions() {
      return excludedDimensions.get().stream().<Identifier>map(Identifier::parse).collect(Collectors.toSet());
   }

   public enum ListMode {
      BLACKLIST,
      WHITELIST;
   }
}

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
   public static final BCCoreConfig.BooleanValue enableOilOceanBiome = new BCCoreConfig.BooleanValue(false);
   public static final BCCoreConfig.BooleanValue enableOilDesertBiome = new BCCoreConfig.BooleanValue(false);
   public static final BCCoreConfig.BooleanValue enableOilBurn = new BCCoreConfig.BooleanValue(true);
   public static final BCCoreConfig.BooleanValue useRfNaming = new BCCoreConfig.BooleanValue(false);
   public static final BCCoreConfig.BooleanValue useFullUnitNames = new BCCoreConfig.BooleanValue(true);
   public static final BCCoreConfig.BooleanValue enableOilGeneration = new BCCoreConfig.BooleanValue(true);
   public static final BCCoreConfig.DoubleValue oilWellGenerationRate = new BCCoreConfig.DoubleValue(1.0);
   public static final BCCoreConfig.BooleanValue enableOilSpouts = new BCCoreConfig.BooleanValue(true);
   public static final BCCoreConfig.BooleanValue spawnOilSprings = new BCCoreConfig.BooleanValue(true);
   public static final BCCoreConfig.IntValue finiteSpoutMinHeight = new BCCoreConfig.IntValue(6);
   public static final BCCoreConfig.IntValue finiteSpoutMaxHeight = new BCCoreConfig.IntValue(12);
   public static final BCCoreConfig.IntValue largeSpoutMinHeight = new BCCoreConfig.IntValue(10);
   public static final BCCoreConfig.IntValue largeSpoutMaxHeight = new BCCoreConfig.IntValue(20);
   public static final BCCoreConfig.DoubleValue mediumOilGenProb = new BCCoreConfig.DoubleValue(0.001);
   public static final BCCoreConfig.DoubleValue largeOilGenProb = new BCCoreConfig.DoubleValue(4.0E-4);
   public static final BCCoreConfig.DoubleValue smallOilGenProb = new BCCoreConfig.DoubleValue(0.02);
   public static final BCCoreConfig.StringListValue forceExcessiveOilBiomes = new BCCoreConfig.StringListValue(List.of());
   /** Biomes flagged for oil-themed world design (advancements); BC 8.0 spawn rolls do not special-case these. */
   public static final BCCoreConfig.StringListValue richSurfaceDepositBiomes = new BCCoreConfig.StringListValue(
      List.of("minecraft:desert", "minecraft:badlands", "minecraft:wooded_badlands")
   );
   /** Biomes with increased oil spawn rate (BC 8.0 default: empty; custom oil biomes are added at runtime). */
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

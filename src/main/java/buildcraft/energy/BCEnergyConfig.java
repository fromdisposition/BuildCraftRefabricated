/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy;

import buildcraft.api.enums.EnumSpring;
import buildcraft.core.BCCoreConfig;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.resources.Identifier;

public class BCEnergyConfig {
   public static final BCCoreConfig.BooleanValue oilIsSticky = new BCCoreConfig.BooleanValue(false);
   public static final BCCoreConfig.BooleanValue enableOilBurn = new BCCoreConfig.BooleanValue(true);
   /** Master switch for all oil worldgen; the per-type toggles below refine it. */
   public static final BCCoreConfig.BooleanValue enableOilGeneration = new BCCoreConfig.BooleanValue(true);
   public static final BCCoreConfig.BooleanValue oilWells = new BCCoreConfig.BooleanValue(true);
   public static final BCCoreConfig.BooleanValue oilDesertFields = new BCCoreConfig.BooleanValue(true);
   /** Also gates normal wells inside ocean biomes. */
   public static final BCCoreConfig.BooleanValue oilOceanFields = new BCCoreConfig.BooleanValue(true);
   /** 0..100, seeded per-position roll on top of the structure_set spacing grid. */
   public static final BCCoreConfig.IntValue oilWellFrequencyPercent = new BCCoreConfig.IntValue(100);
   public static final BCCoreConfig.IntValue oilDesertFieldFrequencyPercent = new BCCoreConfig.IntValue(100);
   public static final BCCoreConfig.IntValue oilOceanFieldFrequencyPercent = new BCCoreConfig.IntValue(100);
   public static final BCCoreConfig.BooleanValue enableOilSprings = new BCCoreConfig.BooleanValue(true);
   public static final BCCoreConfig.BooleanValue enableWaterSprings = new BCCoreConfig.BooleanValue(true);
   public static final BCCoreConfig.StringListValue excludedDimensions = new BCCoreConfig.StringListValue(List.of("minecraft:the_nether", "minecraft:the_end"));
   public static final BCCoreConfig.EnumValue<BCEnergyConfig.ListMode> dimensionListMode = new BCCoreConfig.EnumValue<>(BCEnergyConfig.ListMode.BLACKLIST);

   public static Set<Identifier> getExcludedDimensions() {
      return excludedDimensions.get().stream().<Identifier>map(Identifier::parse).collect(Collectors.toSet());
   }

   public static void refreshWaterSpringFlag() {
      EnumSpring.WATER.canGen = BCCoreConfig.worldGen.get() && enableWaterSprings.get();
   }

   public enum ListMode {
      BLACKLIST,
      WHITELIST;
   }
}

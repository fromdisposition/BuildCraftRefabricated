/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy;

import buildcraft.core.BCCoreConfig;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.resources.Identifier;

public class BCEnergyConfig {
   public static final BCCoreConfig.BooleanValue oilIsSticky = new BCCoreConfig.BooleanValue(false);
   public static final BCCoreConfig.BooleanValue enableRfEngine = new BCCoreConfig.BooleanValue(false);
   public static final BCCoreConfig.BooleanValue enableMjDynamo = new BCCoreConfig.BooleanValue(false);
   /** Master switch: oil patch feature in ocean biomes (default on). */
   public static final BCCoreConfig.BooleanValue enableOilOnWater = new BCCoreConfig.BooleanValue(true);
   public static final BCCoreConfig.BooleanValue enableOilBurn = new BCCoreConfig.BooleanValue(true);
   public static final BCCoreConfig.BooleanValue useRfNaming = new BCCoreConfig.BooleanValue(false);
   public static final BCCoreConfig.BooleanValue useFullUnitNames = new BCCoreConfig.BooleanValue(true);
   public static final BCCoreConfig.BooleanValue enableOilGeneration = new BCCoreConfig.BooleanValue(true);
   public static final BCCoreConfig.BooleanValue enableOilSpouts = new BCCoreConfig.BooleanValue(true);
   public static final BCCoreConfig.BooleanValue spawnOilSprings = new BCCoreConfig.BooleanValue(true);
   public static final BCCoreConfig.StringListValue excludedDimensions = new BCCoreConfig.StringListValue(List.of("minecraft:the_nether", "minecraft:the_end"));
   public static final BCCoreConfig.EnumValue<BCEnergyConfig.ListMode> dimensionListMode = new BCCoreConfig.EnumValue<>(BCEnergyConfig.ListMode.BLACKLIST);

   public static String rfFeKey(String baseKey) {
      return baseKey;
   }

   public static Set<Identifier> getExcludedDimensions() {
      return excludedDimensions.get().stream().<Identifier>map(Identifier::parse).collect(Collectors.toSet());
   }

   public enum ListMode {
      BLACKLIST,
      WHITELIST;
   }
}

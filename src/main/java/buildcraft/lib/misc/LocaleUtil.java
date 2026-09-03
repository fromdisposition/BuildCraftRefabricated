/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import buildcraft.api.mj.MjAPI;
import buildcraft.core.BCCoreConfig;
import buildcraft.lib.BCLibConfig;
import net.minecraft.network.chat.Component;

public final class LocaleUtil {
   private LocaleUtil() {
   }

   public static String localize(String key) {
      return Component.translatable(key).getString();
   }

   public static String localize(String key, Object... args) {
      return Component.translatable(key, args).getString();
   }

   // MJ is BuildCraft's internal unit and is always shown as MJ; external energy (Team Reborn "E") is always shown
   // as E. No cross-conversion in the display -- only the RF Engine / MJ Dynamo bridge the two, so each number
   // reads in its real type instead of the old "everything as E" auto-substitution.
   public static String localizeMj(long microMj) {
      if (BCCoreConfig.hidePower.get()) {
         return "";
      }

      return localize("buildcraft.unit.mj", String.format("%.2f", microMj / 1000000.0));
   }

   public static String localizeMjFlow(long microMjPerTick) {
      if (BCCoreConfig.hidePower.get()) {
         return "";
      }

      long scaled = BCLibConfig.displayTimeGap.get().convertTicksToGap(microMjPerTick);
      String key = BCLibConfig.displayTimeGap.get() == BCLibConfig.TimeGap.SECONDS ? "buildcraft.unit.mj_per_second" : "buildcraft.unit.mj_per_tick";
      return localize(key, String.format("%.2f", scaled / 1000000.0));
   }

   public static String localizeHeat(float heat) {
      return String.format("%.1f", heat);
   }

   public static String localizeRfFlow(int ePerTick) {
      if (BCCoreConfig.hidePower.get()) {
         return "";
      }

      int scaled = (int)BCLibConfig.displayTimeGap.get().convertTicksToGap(ePerTick);
      String key = BCLibConfig.displayTimeGap.get() == BCLibConfig.TimeGap.SECONDS
         ? "buildcraft.unit.energy_per_second"
         : "buildcraft.unit.energy_per_tick";
      return localize(key, scaled, MjAPI.EXTERNAL_ENERGY_UNIT);
   }

   public static String localizeExternalBuffer(int currentE, int maxE) {
      if (BCCoreConfig.hidePower.get()) {
         return "";
      }

      return localize("buildcraft.unit.energy_of", currentE, maxE, MjAPI.EXTERNAL_ENERGY_UNIT);
   }

   public static String localizeFluidFlow(int mbPerTick) {
      if (BCCoreConfig.hideFluid.get()) {
         return "";
      }

      if (BCLibConfig.useBucketsFlow.get()) {
         return localize("buildcraft.unit.buckets_per_second", String.format("%.2f", mbPerTick / 50.0));
      }

      return localize("buildcraft.unit.millibuckets_per_tick", mbPerTick);
   }

   public static String localizeFluidStaticAmount(int fluidAmount, int capacity) {
      if (BCCoreConfig.hideFluid.get()) {
         return "";
      }

      if (fluidAmount <= 0) {
         return capacity > 0
            ? localize("buildcraft.unit.fluid_of", "0", formatFluidAmount(capacity))
            : localize("buildcraft.unit.millibuckets", "0");
      }

      String amount = formatFluidAmount(fluidAmount);
      if (capacity == fluidAmount) {
         return amount;
      }

      return capacity > 0
         ? localize("buildcraft.unit.fluid_of", amount, formatFluidAmount(capacity))
         : localize("buildcraft.unit.millibuckets", amount);
   }

   private static String formatFluidAmount(int milliBuckets) {
      if (BCLibConfig.useBucketsStatic.get()) {
         return String.format("%.2f", milliBuckets / 1000.0);
      }

      return Integer.toString(milliBuckets);
   }
}

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

   public static String localizeMj(long microMj) {
      if (BCCoreConfig.hidePower.get()) {
         return "";
      }

      if (MjAPI.displaysExternalEnergyUnits()) {
         return localizeRf((int)(microMj / MjAPI.getRfConversion().mjPerRf));
      }

      return String.format("%.2f MJ", microMj / 1000000.0);
   }

   public static String localizeMjFlow(long microMjPerTick) {
      if (BCCoreConfig.hidePower.get()) {
         return "";
      }

      if (MjAPI.displaysExternalEnergyUnits()) {
         return localizeRfFlow((int)(microMjPerTick / MjAPI.getRfConversion().mjPerRf));
      }

      long scaled = BCLibConfig.displayTimeGap.get().convertTicksToGap(microMjPerTick);
      String suffix = BCLibConfig.displayTimeGap.get() == BCLibConfig.TimeGap.SECONDS ? " MJ/s" : " MJ/t";
      return String.format("%.2f", scaled / 1000000.0) + suffix;
   }

   public static String localizeHeat(float heat) {
      return String.format("%.1f", heat);
   }

   public static String localizeRfFlow(int ePerTick) {
      if (BCCoreConfig.hidePower.get()) {
         return "";
      }

      if (!MjAPI.displaysExternalEnergyUnits()) {
         return localizeMjFlow(ePerTick * MjAPI.getRfConversion().mjPerRf);
      }

      int scaled = (int)BCLibConfig.displayTimeGap.get().convertTicksToGap(ePerTick);
      String suffix = BCLibConfig.displayTimeGap.get() == BCLibConfig.TimeGap.SECONDS ? " E/s" : " E/t";
      return scaled + suffix;
   }

   public static String localizeRf(int e) {
      if (BCCoreConfig.hidePower.get()) {
         return "";
      }

      if (!MjAPI.displaysExternalEnergyUnits()) {
         return localizeMj(e * MjAPI.getRfConversion().mjPerRf);
      }

      return e + " E";
   }

   public static String localizeExternalBuffer(int currentE, int maxE) {
      if (BCCoreConfig.hidePower.get()) {
         return "";
      }

      if (!MjAPI.isRfAutoConversionEnabled()) {
         return localizeMj(0L) + " / " + localizeMj(0L);
      }

      if (MjAPI.displaysExternalEnergyUnits()) {
         return currentE + " / " + maxE + " E";
      }

      long mjPerE = MjAPI.getRfConversion().mjPerRf;
      return localizeMj(currentE * mjPerE) + " / " + localizeMj(maxE * mjPerE);
   }

   public static String localizeFluidFlow(int mbPerTick) {
      if (BCCoreConfig.hideFluid.get()) {
         return "";
      }

      if (BCLibConfig.useBucketsFlow.get()) {
         return String.format("%.2f B/s", mbPerTick / 50.0);
      }

      return mbPerTick + " mB/t";
   }

   public static String localizeFluidStaticAmount(int fluidAmount, int capacity) {
      if (BCCoreConfig.hideFluid.get()) {
         return "";
      }

      if (fluidAmount <= 0) {
         return capacity > 0 ? "0 / " + formatFluidAmount(capacity) : "0 mB";
      }

      String amount = formatFluidAmount(fluidAmount);
      if (capacity == fluidAmount) {
         return amount;
      }

      return capacity > 0 ? amount + " / " + formatFluidAmount(capacity) : amount + " mB";
   }

   private static String formatFluidAmount(int milliBuckets) {
      if (BCLibConfig.useBucketsStatic.get()) {
         return String.format("%.2f", milliBuckets / 1000.0);
      }

      return Integer.toString(milliBuckets);
   }
}

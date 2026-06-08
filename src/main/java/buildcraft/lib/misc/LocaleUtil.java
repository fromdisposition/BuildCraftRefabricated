/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

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
      return String.format("%.2f MJ", microMj / 1000000.0);
   }

   public static String localizeMjFlow(long microMjPerTick) {
      return String.format("%.2f MJ/t", microMjPerTick / 1000000.0);
   }

   public static String localizeHeat(float heat) {
      return String.format("%.1f", heat);
   }

   public static String localizeRfFlow(int fePerTick) {
      return fePerTick + " FE/t";
   }

   public static String localizeFluidFlow(int mbPerTick) {
      return mbPerTick + " mB/t";
   }
}

/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.mj;

public class MjRfConversion {
   public static final long MAX_MJ_PER_RF = MjAPI.MJ / 5L;
   public static final long MIN_MJ_PER_RF = MjAPI.MJ / 10000L;
   public static final long DEFAULT_MJ_PER_RF = MjAPI.MJ / 10L;
   public final long mjPerRf;
   public final boolean usingDefaultValue;

   private MjRfConversion(long mjPerRf) {
      if (MIN_MJ_PER_RF <= mjPerRf && mjPerRf <= MAX_MJ_PER_RF) {
         this.usingDefaultValue = false;
         this.mjPerRf = mjPerRf;
      } else {
         this.usingDefaultValue = true;
         this.mjPerRf = DEFAULT_MJ_PER_RF;
      }
   }

   public static MjRfConversion createRaw(long mjPerRf) {
      return new MjRfConversion(mjPerRf);
   }

   public static MjRfConversion createParsed(double configMjPerRf) {
      long value = Math.round(configMjPerRf * 10000.0);
      return new MjRfConversion(value * MjAPI.MJ / 10000L);
   }

   public static MjRfConversion createDefault() {
      return new MjRfConversion(-10L);
   }
}

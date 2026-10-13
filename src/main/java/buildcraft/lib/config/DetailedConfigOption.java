/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.config;

public class DetailedConfigOption {
   private final String key;
   private final String defaultValue;

   public DetailedConfigOption(String key, String defaultValue) {
      this.key = key;
      this.defaultValue = defaultValue;
   }

   public float getAsFloat() {
      try {
         return Float.parseFloat(this.defaultValue);
      } catch (NumberFormatException e) {
         return 0.725F;
      }
   }

   public int getAsInt() {
      try {
         return Integer.parseInt(this.defaultValue);
      } catch (NumberFormatException e) {
         return 0;
      }
   }

   public String get() {
      return this.defaultValue;
   }
}

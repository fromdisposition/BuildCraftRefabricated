/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders;

import com.google.gson.JsonObject;

public final class BCBuildersConfig {
   public static final BCBuildersConfig.IntValue quarryFrameMinHeight = new BCBuildersConfig.IntValue(4);
   public static final BCBuildersConfig.IntValue quarryMaxTasksPerTick = new BCBuildersConfig.IntValue(4);
   public static final BCBuildersConfig.IntValue quarryTaskPowerDivisor = new BCBuildersConfig.IntValue(2);
   public static final BCBuildersConfig.DoubleValue quarryMaxFrameMoveSpeed = new BCBuildersConfig.DoubleValue(0.0);
   public static final BCBuildersConfig.DoubleValue quarryMaxBlockMineRate = new BCBuildersConfig.DoubleValue(0.0);

   private BCBuildersConfig() {
   }

   @Deprecated
   public static void ensureLoaded() {
   }

   @Deprecated
   public static void buildGeneral(Object builder) {
   }

   public static void applyQuarry(JsonObject quarry) {
      if (quarry != null) {
         if (quarry.has("quarryFrameMinHeight")) {
            quarryFrameMinHeight.set(clampInt(quarry.get("quarryFrameMinHeight").getAsInt(), 1, 256));
         }

         if (quarry.has("quarryMaxTasksPerTick")) {
            quarryMaxTasksPerTick.set(clampInt(quarry.get("quarryMaxTasksPerTick").getAsInt(), 1, 20));
         }

         if (quarry.has("quarryTaskPowerDivisor")) {
            quarryTaskPowerDivisor.set(clampInt(quarry.get("quarryTaskPowerDivisor").getAsInt(), 0, 100));
         }

         if (quarry.has("quarryMaxFrameMoveSpeed")) {
            quarryMaxFrameMoveSpeed.set(clampDouble(quarry.get("quarryMaxFrameMoveSpeed").getAsDouble(), 0.0, 5120.0));
         }

         if (quarry.has("quarryMaxBlockMineRate")) {
            quarryMaxBlockMineRate.set(clampDouble(quarry.get("quarryMaxBlockMineRate").getAsDouble(), 0.0, 1000.0));
         }
      }
   }

   private static int clampInt(int value, int min, int max) {
      return Math.max(min, Math.min(max, value));
   }

   private static double clampDouble(double value, double min, double max) {
      return Math.max(min, Math.min(max, value));
   }

   public static final class DoubleValue {
      private double value;

      public DoubleValue(double value) {
         this.value = value;
      }

      public double get() {
         return this.value;
      }

      public void set(double value) {
         this.value = value;
      }
   }

   public static final class IntValue {
      private int value;

      public IntValue(int value) {
         this.value = value;
      }

      public int get() {
         return this.value;
      }

      public void set(int value) {
         this.value = value;
      }
   }
}

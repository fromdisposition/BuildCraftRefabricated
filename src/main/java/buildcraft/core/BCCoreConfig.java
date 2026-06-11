/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core;

import java.util.ArrayList;
import java.util.List;

public final class BCCoreConfig {
   public static final BCCoreConfig.BooleanValue worldGen = new BCCoreConfig.BooleanValue(true);
   public static final BCCoreConfig.BooleanValue hidePower = new BCCoreConfig.BooleanValue(false);
   public static final BCCoreConfig.BooleanValue hideFluid = new BCCoreConfig.BooleanValue(false);
   public static final BCCoreConfig.BooleanValue minePlayerProtected = new BCCoreConfig.BooleanValue(false);
   public static final BCCoreConfig.BooleanValue pumpsConsumeWater = new BCCoreConfig.BooleanValue(false);
   public static final BCCoreConfig.IntValue markerMaxDistance = new BCCoreConfig.IntValue(64);
   public static final BCCoreConfig.IntValue pumpMaxDistance = new BCCoreConfig.IntValue(64);
   public static final BCCoreConfig.IntValue networkUpdateRate = new BCCoreConfig.IntValue(10);
   public static final BCCoreConfig.DoubleValue miningMultiplier = new BCCoreConfig.DoubleValue(1.0);
   public static final BCCoreConfig.IntValue miningMaxDepth = new BCCoreConfig.IntValue(512);

   private BCCoreConfig() {
   }

   public static void buildGeneral(Object builder) {
   }

   public static void buildWorldgen(Object builder) {
   }

   public static final class BooleanValue {
      private boolean value;

      public BooleanValue(boolean value) {
         this.value = value;
      }

      public boolean get() {
         return this.value;
      }

      public void set(boolean value) {
         this.value = value;
      }
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

   public static final class EnumValue<T extends Enum<T>> {
      private T value;

      public EnumValue(T value) {
         this.value = value;
      }

      public T get() {
         return this.value;
      }

      public void set(T value) {
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

   public static final class StringListValue {
      private List<String> value;

      public StringListValue(List<String> value) {
         this.value = new ArrayList<>(value);
      }

      public List<? extends String> get() {
         return this.value;
      }

      public void set(List<String> value) {
         this.value = new ArrayList<>(value);
      }
   }
}

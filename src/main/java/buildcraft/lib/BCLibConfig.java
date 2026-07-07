/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib;

import buildcraft.lib.chunkload.IChunkLoadingTile;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;

public final class BCLibConfig {
   public static final BCLibConfig.ColorBlindMode COLOR_BLIND_MODE = BCLibConfig.ColorBlindMode.AUTO;
   public static final BCLibConfig.TimeGap DISPLAY_TIME_GAP = BCLibConfig.TimeGap.SECONDS;
   public static final BCLibConfig.RenderRotation ROTATE_TRAVELING_ITEMS = BCLibConfig.RenderRotation.ENABLED;
   public static final BCLibConfig.ChunkLoaderLevel CHUNK_LOADING_LEVEL = BCLibConfig.ChunkLoaderLevel.SELF_TILES;
   public static final BCLibConfig.EnumValue<BCLibConfig.ColorBlindMode> colorBlindMode = new BCLibConfig.EnumValue<>(COLOR_BLIND_MODE);
   public static final BCLibConfig.DoubleValue mjRfConversionAmount = new BCLibConfig.DoubleValue(0.1);
   public static final BCLibConfig.BooleanValue canEnginesExplode = new BCLibConfig.BooleanValue(true);
   public static final BCLibConfig.BooleanValue useColouredLabels = new BCLibConfig.BooleanValue(true);
   public static final BCLibConfig.BooleanValue useHighContrastLabelColours = new BCLibConfig.BooleanValue(false);
   public static final BCLibConfig.BooleanValue useBucketsStatic = new BCLibConfig.BooleanValue(true);
   public static final BCLibConfig.BooleanValue useBucketsFlow = new BCLibConfig.BooleanValue(true);
   public static final BCLibConfig.BooleanValue useLongLocalizedName = new BCLibConfig.BooleanValue(true);
   public static final BCLibConfig.BooleanValue useSwappableSprites = new BCLibConfig.BooleanValue(true);
   public static final BCLibConfig.BooleanValue enableAnimatedSprites = new BCLibConfig.BooleanValue(true);
   public static final BCLibConfig.BooleanValue guideShowDetail = new BCLibConfig.BooleanValue(false);
   public static final BCLibConfig.IntValue itemLifespan = new BCLibConfig.IntValue(60);
   public static final BCLibConfig.IntValue guideItemSearchLimit = new BCLibConfig.IntValue(10000);
   public static final BCLibConfig.IntValue maxGuideSearchCount = new BCLibConfig.IntValue(1200);
   public static final BCLibConfig.EnumValue<BCLibConfig.TimeGap> displayTimeGap = new BCLibConfig.EnumValue<>(DISPLAY_TIME_GAP);
   public static final BCLibConfig.EnumValue<BCLibConfig.RenderRotation> rotateTravelingItems = new BCLibConfig.EnumValue<>(ROTATE_TRAVELING_ITEMS);
   public static final BCLibConfig.EnumValue<BCLibConfig.ChunkLoaderLevel> chunkLoadingLevel = new BCLibConfig.EnumValue<>(CHUNK_LOADING_LEVEL);

   private BCLibConfig() {
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

   public enum ChunkLoaderLevel {
      NONE,
      STRICT_TILES,
      SELF_TILES,
      ALL_TILES;

      public boolean canLoad(IChunkLoadingTile.LoadType loadType) {
         return switch (this) {
            case NONE -> false;
            case STRICT_TILES -> loadType == IChunkLoadingTile.LoadType.HARD;
            case SELF_TILES, ALL_TILES -> loadType != null;
         };
      }
   }

   public enum ColorBlindMode {
      AUTO,
      ON,
      OFF;
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

   public static final class EnumValue<T> {
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

   public enum RenderRotation {
      DISABLED {
         @Override
         public Direction changeFacing(Direction dir) {
            return Direction.EAST;
         }
      },
      HORIZONTALS_ONLY {
         @Override
         public Direction changeFacing(Direction dir) {
            return dir.getAxis() == Axis.Y ? Direction.EAST : dir;
         }
      },
      ENABLED {
         @Override
         public Direction changeFacing(Direction dir) {
            return dir;
         }
      };

      public abstract Direction changeFacing(Direction dir);
   }

   public enum TimeGap {
      TICKS(1),
      SECONDS(20);

      private final int ticksInGap;

      TimeGap(int ticksInGap) {
         this.ticksInGap = ticksInGap;
      }

      public long convertTicksToGap(long ticks) {
         return ticks * this.ticksInGap;
      }
   }
}

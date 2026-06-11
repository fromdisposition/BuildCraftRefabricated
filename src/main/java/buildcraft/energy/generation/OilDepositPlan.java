/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.generation;

import buildcraft.energy.BCEnergyConfig;
import org.jspecify.annotations.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;

/**
 * Immutable owner-chunk deposit plan. All surface deposits share the BC 8.0 fractal tendril;
 * {@link DepositType} selects tendril size and optional underground sphere, geyser, spring.
 *
 * <p>Deposit tiers (BC 8.0 spawn order: large → medium → small):
 * <ul>
 *   <li>LAND_LAKE / OCEAN_PATCH — small tendril, surface only; small roll requires {@code surfaceDepositBiomes}</li>
 *   <li>LAND_FOUNTAIN / OCEAN_FOUNTAIN — small tendril + sphere r4–6 + finite spout</li>
 *   <li>LAND_LARGE / OCEAN_LARGE — large tendril r25–44 + sphere r8–16 + tall spout + spring</li>
 * </ul>
 * LAND_LAKE and LAND_FOUNTAIN require a flat owner chunk; OCEAN types do not.
 */
public final class OilDepositPlan {
   public static final int SURFACE_DEPTH = 1;

   public final int ownerChunkX;
   public final int ownerChunkZ;
   public final int anchorX;
   public final int anchorZ;
   public final DepositType type;

   public final int lakeRadius;
   public final int tendrilRadius;
   public final BlockPos patternStart;
   public final boolean[][] surfacePattern;

   public final int surfaceCenterY;
   public final SurfacePlacement surfacePlacement;

   @Nullable
   public final Integer wellY;
   @Nullable
   public final Integer wellRadius;
   public final int spoutSegmentHeight;
   public final int spoutRadius;
   public final boolean hasSpring;
   @Nullable
   public final BlockPos springPos;

   public final int estimatedSourceCount;

   private final int patternMinX;
   private final int patternMaxX;
   private final int patternMinZ;
   private final int patternMaxZ;

   static OilDepositPlan build(
      WorldGenLevel level,
      int ownerChunkX,
      int ownerChunkZ,
      DepositType type,
      BlockPos anchor,
      SurfacePlacement surfacePlacement,
      int lakeRadius,
      int tendrilRadius,
      boolean[][] surfacePattern,
      RandomSource random
   ) {
      BlockPos patternStart = anchor.offset(-tendrilRadius, 0, -tendrilRadius);

      Integer wellY = null;
      Integer wellRadius = null;
      int spoutSegmentHeight = 0;
      int spoutRadius = 0;
      boolean hasSpring = false;
      BlockPos springPos = null;

      if (type.hasUndergroundPool()) {
         wellY = level.getMinY() + 25 + random.nextInt(10);
         wellRadius = type.rollWellRadius(random);
      }

      if (type.hasSpout() && BCEnergyConfig.enableOilSpouts.get()) {
         spoutRadius = type.spoutRadius();
         spoutSegmentHeight = type.rollSpoutSegmentHeight(random);
      }

      if (type.allowsSpring() && BCEnergyConfig.spawnOilSprings.get()) {
         hasSpring = true;
         springPos = new BlockPos(anchor.getX(), level.getMinY(), anchor.getZ());
      }

      int sourceCount = estimateSourceCount(level, surfacePattern, wellY, wellRadius, spoutSegmentHeight, spoutRadius, hasSpring);

      return new OilDepositPlan(
         ownerChunkX,
         ownerChunkZ,
         anchor.getX(),
         anchor.getZ(),
         type,
         lakeRadius,
         tendrilRadius,
         patternStart,
         surfacePattern,
         anchor.getY(),
         surfacePlacement,
         wellY,
         wellRadius,
         spoutSegmentHeight,
         spoutRadius,
         hasSpring,
         springPos,
         sourceCount
      );
   }

   OilDepositPlan(
      int ownerChunkX,
      int ownerChunkZ,
      int anchorX,
      int anchorZ,
      DepositType type,
      int lakeRadius,
      int tendrilRadius,
      BlockPos patternStart,
      boolean[][] surfacePattern,
      int surfaceCenterY,
      SurfacePlacement surfacePlacement,
      @Nullable Integer wellY,
      @Nullable Integer wellRadius,
      int spoutSegmentHeight,
      int spoutRadius,
      boolean hasSpring,
      @Nullable BlockPos springPos,
      int estimatedSourceCount
   ) {
      this.ownerChunkX = ownerChunkX;
      this.ownerChunkZ = ownerChunkZ;
      this.anchorX = anchorX;
      this.anchorZ = anchorZ;
      this.type = type;
      this.lakeRadius = lakeRadius;
      this.tendrilRadius = tendrilRadius;
      this.patternStart = patternStart;
      this.surfacePattern = surfacePattern;
      this.surfaceCenterY = surfaceCenterY;
      this.surfacePlacement = surfacePlacement;
      this.wellY = wellY;
      this.wellRadius = wellRadius;
      this.spoutSegmentHeight = spoutSegmentHeight;
      this.spoutRadius = spoutRadius;
      this.hasSpring = hasSpring;
      this.springPos = springPos;
      this.estimatedSourceCount = estimatedSourceCount;
      this.patternMinX = anchorX - tendrilRadius;
      this.patternMaxX = anchorX + tendrilRadius;
      this.patternMinZ = anchorZ - tendrilRadius;
      this.patternMaxZ = anchorZ + tendrilRadius;
   }

   public boolean intersectsSlice(int sliceChunkX, int sliceChunkZ) {
      int sliceMinX = sliceChunkX << 4;
      int sliceMaxX = sliceMinX + 15;
      int sliceMinZ = sliceChunkZ << 4;
      int sliceMaxZ = sliceMinZ + 15;

      if (rangesOverlap(sliceMinX, sliceMaxX, patternMinX, patternMaxX)
         && rangesOverlap(sliceMinZ, sliceMaxZ, patternMinZ, patternMaxZ)) {
         return true;
      }

      if (wellY != null && wellRadius != null) {
         int reach = type.maxWellReach();
         if (rangesOverlap(sliceMinX, sliceMaxX, anchorX - reach, anchorX + reach)
            && rangesOverlap(sliceMinZ, sliceMaxZ, anchorZ - reach, anchorZ + reach)) {
            return true;
         }

         if (rangesOverlap(sliceMinX, sliceMaxX, anchorX - spoutRadius, anchorX + spoutRadius)
            && rangesOverlap(sliceMinZ, sliceMaxZ, anchorZ - spoutRadius, anchorZ + spoutRadius)) {
            return true;
         }
      }

      return hasSpring && sliceChunkX == ownerChunkX && sliceChunkZ == ownerChunkZ;
   }

   static boolean rangesOverlap(int aMin, int aMax, int bMin, int bMax) {
      return aMin <= bMax && bMin <= aMax;
   }

   private static int estimateSourceCount(
      WorldGenLevel level,
      boolean[][] pattern,
      @Nullable Integer wellY,
      @Nullable Integer wellRadius,
      int spoutSegmentHeight,
      int spoutRadius,
      boolean hasSpring
   ) {
      int total = OilTendrilPattern.countCells(pattern) * SURFACE_DEPTH;
      if (wellRadius == null) {
         return total;
      }

      total += estimateSphereBlocks(wellRadius);
      total += estimateSpoutBlocks(spoutSegmentHeight, spoutRadius);
      if (hasSpring && wellY != null) {
         total += estimateTubeBlocks(Math.max(0, wellY - (level.getMinY() + 2)), spoutRadius);
      }
      return total;
   }

   private static int estimateSphereBlocks(int radius) {
      return (int)Math.round(4.0 / 3.0 * Math.PI * radius * radius * radius);
   }

   private static int estimateTubeBlocks(int length, int radius) {
      return (int)Math.round(Math.PI * radius * radius * (length + 1));
   }

   private static int estimateSpoutBlocks(int segmentHeight, int spoutRadius) {
      int estimate = 0;
      for (int r = spoutRadius; r >= 0; r--) {
         int side = 2 * r + 1;
         estimate += side * side * segmentHeight;
      }
      estimate += (2 * spoutRadius + 1) * (2 * spoutRadius + 1) * 16;
      return estimate;
   }

   /** Surface tendril size tier. Small = lake r2 / tendril r5–14; large = lake r4 / tendril r25–44. */
   public enum TendrilSize {
      SMALL(2, 5, 10),
      LARGE(4, 25, 20);

      private final int lakeRadius;
      private final int tendrilBase;
      private final int tendrilSpread;

      TendrilSize(int lakeRadius, int tendrilBase, int tendrilSpread) {
         this.lakeRadius = lakeRadius;
         this.tendrilBase = tendrilBase;
         this.tendrilSpread = tendrilSpread;
      }

      public int rollLakeRadius() {
         return lakeRadius;
      }

      public int rollTendrilRadius(RandomSource rand) {
         return tendrilBase + rand.nextInt(tendrilSpread);
      }
   }

   public enum DepositType {
      LAND_LAKE(TendrilSize.SMALL, false, false, false),
      LAND_FOUNTAIN(TendrilSize.SMALL, true, true, false),
      LAND_LARGE(TendrilSize.LARGE, true, true, true),
      OCEAN_PATCH(TendrilSize.SMALL, false, false, false),
      OCEAN_FOUNTAIN(TendrilSize.SMALL, true, true, false),
      OCEAN_LARGE(TendrilSize.LARGE, true, true, true);

      private final TendrilSize tendrilSize;
      private final boolean underground;
      private final boolean spout;
      private final boolean spring;

      DepositType(TendrilSize tendrilSize, boolean underground, boolean spout, boolean spring) {
         this.tendrilSize = tendrilSize;
         this.underground = underground;
         this.spout = spout;
         this.spring = spring;
      }

      public TendrilSize tendrilSize() {
         return tendrilSize;
      }

      public boolean isLand() {
         return name().startsWith("LAND_");
      }

      public boolean hasUndergroundPool() {
         return underground;
      }

      public boolean hasSpout() {
         return spout;
      }

      public boolean allowsSpring() {
         return spring;
      }

      /** Small land deposits only generate on mostly flat owner chunks. */
      public boolean requiresFlatLandSite() {
         return this == LAND_LAKE || this == LAND_FOUNTAIN;
      }

      public int rollWellRadius(RandomSource rand) {
         return switch (this) {
            case LAND_FOUNTAIN, OCEAN_FOUNTAIN -> 4 + rand.nextInt(3);
            case LAND_LARGE, OCEAN_LARGE -> 8 + rand.nextInt(9);
            default -> throw new IllegalStateException("No well for " + this);
         };
      }

      public int maxWellReach() {
         return switch (this) {
            case LAND_LARGE, OCEAN_LARGE -> 16;
            case LAND_FOUNTAIN, OCEAN_FOUNTAIN -> 7;
            default -> 0;
         };
      }

      public boolean largeSpout() {
         return this == LAND_LARGE || this == OCEAN_LARGE;
      }

      public int spoutRadius() {
         return largeSpout() ? 1 : 0;
      }

      public int rollSpoutSegmentHeight(RandomSource rand) {
         int min = largeSpout() ? BCEnergyConfig.largeSpoutMinHeight.get() : BCEnergyConfig.finiteSpoutMinHeight.get();
         int max = largeSpout() ? BCEnergyConfig.largeSpoutMaxHeight.get() : BCEnergyConfig.finiteSpoutMaxHeight.get();
         return max <= min ? min : min + rand.nextInt(max - min);
      }
   }

   /** Per-column surface Y: solid ground on land, water block on ocean. */
   public enum SurfacePlacement {
      FLAT_LAND,
      OCEAN
   }
}

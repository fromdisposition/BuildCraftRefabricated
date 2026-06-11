/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.generation;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;

/**
 * Immutable owner-chunk deposit plan. Built only after a successful roll and slice-bounds prefilter.
 * Geometry matches BC 8.0; slice rendering is delegated to {@link OilSliceRenderer}.
 */
public final class OilDepositPlan {
   public final int ownerChunkX;
   public final int ownerChunkZ;
   public final int anchorX;
   public final int anchorZ;
   public final OilDepositPlan.DepositType type;

   public final int lakeRadius;
   public final int tendrilRadius;
   public final int tendrilDepth;
   public final BlockPos patternStart;
   public final boolean[][] tendrilPattern;

   public final int surfaceCenterY;

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

   private final int tendrilMinX;
   private final int tendrilMaxX;
   private final int tendrilMinZ;
   private final int tendrilMaxZ;

   OilDepositPlan(
      int ownerChunkX,
      int ownerChunkZ,
      int anchorX,
      int anchorZ,
      DepositType type,
      int lakeRadius,
      int tendrilRadius,
      int tendrilDepth,
      BlockPos patternStart,
      boolean[][] tendrilPattern,
      int surfaceCenterY,
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
      this.tendrilDepth = tendrilDepth;
      this.patternStart = patternStart;
      this.tendrilPattern = tendrilPattern;
      this.surfaceCenterY = surfaceCenterY;
      this.wellY = wellY;
      this.wellRadius = wellRadius;
      this.spoutSegmentHeight = spoutSegmentHeight;
      this.spoutRadius = spoutRadius;
      this.hasSpring = hasSpring;
      this.springPos = springPos;
      this.estimatedSourceCount = estimatedSourceCount;
      this.tendrilMinX = anchorX - tendrilRadius;
      this.tendrilMaxX = anchorX + tendrilRadius;
      this.tendrilMinZ = anchorZ - tendrilRadius;
      this.tendrilMaxZ = anchorZ + tendrilRadius;
   }

   public boolean intersectsSlice(int sliceChunkX, int sliceChunkZ) {
      int sliceMinX = sliceChunkX << 4;
      int sliceMaxX = sliceMinX + 15;
      int sliceMinZ = sliceChunkZ << 4;
      int sliceMaxZ = sliceMinZ + 15;

      if (horizontalRangesOverlap(sliceMinX, sliceMaxX, tendrilMinX, tendrilMaxX)
         && horizontalRangesOverlap(sliceMinZ, sliceMaxZ, tendrilMinZ, tendrilMaxZ)) {
         return true;
      }

      if (wellY != null && wellRadius != null) {
         int wellMinX = anchorX - wellRadius;
         int wellMaxX = anchorX + wellRadius;
         int wellMinZ = anchorZ - wellRadius;
         int wellMaxZ = anchorZ + wellRadius;
         if (horizontalRangesOverlap(sliceMinX, sliceMaxX, wellMinX, wellMaxX)
            && horizontalRangesOverlap(sliceMinZ, sliceMaxZ, wellMinZ, wellMaxZ)) {
            return true;
         }

         int spoutMaxX = anchorX + spoutRadius;
         int spoutMinX = anchorX - spoutRadius;
         int spoutMaxZ = anchorZ + spoutRadius;
         int spoutMinZ = anchorZ - spoutRadius;
         if (horizontalRangesOverlap(sliceMinX, sliceMaxX, spoutMinX, spoutMaxX)
            && horizontalRangesOverlap(sliceMinZ, sliceMaxZ, spoutMinZ, spoutMaxZ)) {
            return true;
         }
      }

      return hasSpring && sliceChunkX == ownerChunkX && sliceChunkZ == ownerChunkZ;
   }

   private static boolean horizontalRangesOverlap(int aMin, int aMax, int bMin, int bMax) {
      return aMin <= bMax && bMin <= aMax;
   }

   public enum DepositType {
      LARGE,
      MEDIUM,
      LAKE
   }
}

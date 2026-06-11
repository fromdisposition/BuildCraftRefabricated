/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.generation;

import buildcraft.energy.BCEnergyConfig;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.data.Box;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Fast slice-only renderer for {@link OilDepositPlan}. Only writes into the decorating chunk bounds.
 */
public final class OilSliceRenderer {
   private static final int GEN_FLAGS = 0;
   private static final int OCEAN_SPREAD_CLEAR_HEIGHT = 5;

   private OilSliceRenderer() {
   }

   public static boolean renderSlice(WorldGenLevel level, OilDepositPlan plan, int sliceChunkX, int sliceChunkZ) {
      if (!plan.intersectsSlice(sliceChunkX, sliceChunkZ)) {
         return false;
      }

      Box sliceBounds = chunkBounds(level, sliceChunkX, sliceChunkZ);
      boolean placed = false;
      placed |= renderTendril(level, plan, sliceBounds);
      if (plan.wellY != null && plan.wellRadius != null) {
         placed |= renderSphere(level, plan.anchorX, plan.wellY, plan.anchorZ, plan.wellRadius, sliceBounds);
         if (BCEnergyConfig.enableOilSpouts.get()) {
            placed |= renderSpout(level, plan, sliceBounds);
         }

         if (plan.hasSpring && plan.springPos != null && sliceChunkX == plan.ownerChunkX && sliceChunkZ == plan.ownerChunkZ) {
            placed |= renderSpringTube(level, plan, sliceBounds);
            placed |= renderSpring(level, plan);
         }
      }

      return placed;
   }

   private static Box chunkBounds(WorldGenLevel level, int chunkX, int chunkZ) {
      int baseX = chunkX << 4;
      int baseZ = chunkZ << 4;
      return new Box(new BlockPos(baseX, level.getMinY(), baseZ), new BlockPos(baseX + 15, level.getMaxY(), baseZ + 15));
   }

   private static boolean renderTendril(WorldGenLevel level, OilDepositPlan plan, Box sliceBounds) {
      Box tendrilBox = OilGenStructure.tendrilBounds(plan.patternStart, plan.tendrilPattern, plan.tendrilDepth, plan.surfaceCenterY);
      Box intersect = tendrilBox.getIntersect(sliceBounds);
      if (intersect == null) {
         return false;
      }

      boolean placed = false;
      int patternOriginX = plan.patternStart.getX();
      int patternOriginZ = plan.patternStart.getZ();

      for (int x = intersect.min().getX(); x <= intersect.max().getX(); x++) {
         int px = x - patternOriginX;

         for (int z = intersect.min().getZ(); z <= intersect.max().getZ(); z++) {
            int pz = z - patternOriginZ;
            if (!isPatternSet(plan.tendrilPattern, px, pz)) {
               continue;
            }

            BlockPos upper = OilGenStructure.findTerrainUpper(level, x, z);
            clearSpreadSpaceAbove(level, upper, OCEAN_SPREAD_CLEAR_HEIGHT);

            for (int y = 0; y < plan.tendrilDepth; y++) {
               if (placeOilIfInSlice(level, upper.below(y), sliceBounds)) {
                  placed = true;
               }
            }
         }
      }

      return placed;
   }

   private static boolean renderSphere(
      WorldGenLevel level, int centerX, int centerY, int centerZ, int radius, Box sliceBounds
   ) {
      double radiusSq = radius * radius + 0.01;
      int minX = Math.max(sliceBounds.min().getX(), centerX - radius);
      int maxX = Math.min(sliceBounds.max().getX(), centerX + radius);
      int minY = Math.max(sliceBounds.min().getY(), centerY - radius);
      int maxY = Math.min(sliceBounds.max().getY(), centerY + radius);
      int minZ = Math.max(sliceBounds.min().getZ(), centerZ - radius);
      int maxZ = Math.min(sliceBounds.max().getZ(), centerZ + radius);

      boolean placed = false;

      for (int y = minY; y <= maxY; y++) {
         int dy = y - centerY;

         for (int x = minX; x <= maxX; x++) {
            int dx = x - centerX;

            for (int z = minZ; z <= maxZ; z++) {
               int dz = z - centerZ;
               if (dx * dx + dy * dy + dz * dz <= radiusSq) {
                  if (placeOilIfInSlice(level, new BlockPos(x, y, z), sliceBounds)) {
                     placed = true;
                  }
               }
            }
         }
      }

      return placed;
   }

   private static boolean renderVerticalTube(
      WorldGenLevel level, int centerX, int baseY, int centerZ, int length, int radius, Box sliceBounds
   ) {
      if (length <= 0) {
         return false;
      }

      double radiusSq = radius * radius;
      int minX = Math.max(sliceBounds.min().getX(), centerX - radius);
      int maxX = Math.min(sliceBounds.max().getX(), centerX + radius);
      int minZ = Math.max(sliceBounds.min().getZ(), centerZ - radius);
      int maxZ = Math.min(sliceBounds.max().getZ(), centerZ + radius);
      int minY = Math.max(sliceBounds.min().getY(), baseY);
      int maxY = Math.min(sliceBounds.max().getY(), baseY + length);

      boolean placed = false;

      for (int y = minY; y <= maxY; y++) {
         for (int x = minX; x <= maxX; x++) {
            int dx = x - centerX;

            for (int z = minZ; z <= maxZ; z++) {
               int dz = z - centerZ;
               if (dx * dx + dz * dz <= radiusSq) {
                  if (placeOilIfInSlice(level, new BlockPos(x, y, z), sliceBounds)) {
                     placed = true;
                  }
               }
            }
         }
      }

      return placed;
   }

   private static boolean renderSpout(WorldGenLevel level, OilDepositPlan plan, Box sliceBounds) {
      if (plan.wellY == null) {
         return false;
      }

      int anchorChunkX = plan.anchorX >> 4;
      int anchorChunkZ = plan.anchorZ >> 4;
      int sliceChunkX = sliceBounds.min().getX() >> 4;
      int sliceChunkZ = sliceBounds.min().getZ() >> 4;
      if (anchorChunkX != sliceChunkX || anchorChunkZ != sliceChunkZ) {
         return false;
      }

      BlockPos start = new BlockPos(plan.anchorX, plan.wellY, plan.anchorZ);
      boolean placed = false;
      BlockPos worldTop = resolveSpoutSurface(level, start);
      int tubeLen = Math.max(0, worldTop.getY() - start.getY());
      placed |= renderVerticalTube(level, plan.anchorX, start.getY(), plan.anchorZ, tubeLen, plan.spoutRadius, sliceBounds);

      BlockPos base = worldTop;
      for (int r = plan.spoutRadius; r >= 0; r--) {
         placed |= renderVerticalTube(level, base.getX(), base.getY(), base.getZ(), plan.spoutSegmentHeight, r, sliceBounds);
         base = base.offset(0, plan.spoutSegmentHeight, 0);
      }

      return placed;
   }

   private static boolean renderSpringTube(WorldGenLevel level, OilDepositPlan plan, Box sliceBounds) {
      if (plan.wellY == null) {
         return false;
      }

      return renderVerticalTube(level, plan.anchorX, 1, plan.anchorZ, plan.wellY, plan.spoutRadius, sliceBounds);
   }

   private static boolean renderSpring(WorldGenLevel level, OilDepositPlan plan) {
      if (plan.springPos == null) {
         return false;
      }

      OilGenerator.createSpring(plan.springPos).generate(level, plan.estimatedSourceCount);
      return true;
   }

   private static BlockPos resolveSpoutSurface(LevelAccessor level, BlockPos start) {
      int topY = level.getMaxY();
      if (level instanceof WorldGenLevel wg) {
         // Match BC 8.0 semantics: stop at first non-air fluid/solid from world surface downward.
         topY = Math.min(topY, wg.getHeight(Heightmap.Types.WORLD_SURFACE, start.getX(), start.getZ()));
      }

      for (int y = topY; y >= start.getY(); y--) {
         BlockPos probe = new BlockPos(start.getX(), y, start.getZ());
         BlockState state = level.getBlockState(probe);
         if (!state.isAir() && (BlockUtil.getFluidWithFlowing(state.getBlock()) != null || BlockUtil.blocksMotion(state))) {
            return probe;
         }
      }

      return start;
   }

   private static void clearSpreadSpaceAbove(WorldGenLevel level, BlockPos surface, int height) {
      for (int y = 1; y <= height; y++) {
         BlockPos above = surface.above(y);
         BlockState state = level.getBlockState(above);
         if (state.isAir() || state.canBeReplaced()) {
            level.setBlock(above, Blocks.AIR.defaultBlockState(), GEN_FLAGS);
         }
      }
   }

   private static boolean placeOilIfInSlice(WorldGenLevel level, BlockPos pos, Box sliceBounds) {
      if (!sliceBounds.contains(pos)) {
         return false;
      }

      OilGenStructure.setOil(level, pos);
      return true;
   }

   private static boolean isPatternSet(boolean[][] pattern, int x, int z) {
      return x >= 0 && x < pattern.length && z >= 0 && z < pattern[x].length && pattern[x][z];
   }
}

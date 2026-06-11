/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.generation;

import buildcraft.api.core.BCLog;
import buildcraft.core.BCCoreBlocks;
import buildcraft.energy.tile.TileSpringOil;
import buildcraft.fabric.BCEnergyFluidsFabric;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.data.Box;
import buildcraft.fabric.fluid.OilFluidDebugLog;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class OilGenStructure {
   private static final int SURFACE_BOX_Y_MARGIN = 8;
   private static final int OCEAN_SPREAD_CLEAR_HEIGHT = 5;
   /** BC 8.0: clear one block above land surface oil. */
   private static final int LAND_SPREAD_CLEAR_HEIGHT = 1;
   /** Max surface Y spread across the owner chunk for small land deposits. */
   private static final int LAND_FLAT_CHUNK_MAX_SPREAD = 3;
   /** Max surface Y spread inside the tendril footprint. */
   private static final int LAND_FLAT_TENDRIL_MAX_SPREAD = 2;
   /** Min valid ground columns in the owner chunk (out of 256). */
   private static final int LAND_FLAT_CHUNK_MIN_VALID = 220;
   /** Min valid ground columns inside the tendril square (fraction). */
   private static final double LAND_FLAT_TENDRIL_MIN_VALID = 0.90;
   /** Land preflight: min placeable fraction of tendril pattern before accepting a deposit. */
   private static final double LAND_SMALL_MIN_PLACE_RATIO = 0.35;
   private static final int LAND_SMALL_MIN_PLACEABLE = 12;
   private static final double LAND_LARGE_MIN_PLACE_RATIO = 0.28;
   private static final int LAND_LARGE_MIN_PLACEABLE = 35;
   /** Notify clients + neighbors so fluid ticks and gravity updates run after placement. */
   public static final int GEN_FLAGS = Block.UPDATE_NEIGHBORS | Block.UPDATE_CLIENTS;

   private OilGenStructure() {
   }

   public static void setOil(LevelAccessor level, BlockPos pos) {
      BlockState before = level.getBlockState(pos);
      BlockState oil = BCEnergyFluidsFabric.oilSourceBlockStateForLevel(level instanceof Level l ? l : null);
      if (oil == null) {
         oil = BCEnergyFluidsFabric.OIL_COOL.still().defaultFluidState().createLegacyBlock();
      }

      level.setBlock(pos, oil, GEN_FLAGS);

      // #region agent log
      boolean replacedFluid = !before.getFluidState().isEmpty() || before.is(Blocks.LAVA);
      if (replacedFluid) {
         BlockState after = level.getBlockState(pos);
         OilFluidDebugLog.log(
            "OilGenStructure.java:setOil",
            "oil replaced existing fluid/block",
            "H3",
            Map.of(
               "pos",
               pos.toShortString(),
               "before",
               BuiltInRegistries.BLOCK.getKey(before.getBlock()).toString(),
               "after",
               BuiltInRegistries.BLOCK.getKey(after.getBlock()).toString(),
               "levelClass",
               level.getClass().getSimpleName(),
               "fluidLevel",
               after.getFluidState().getAmount()
            )
         );
      }
      // #endregion
   }

   public static OilDepositPlan.SurfacePlacement surfacePlacementFor(Holder<Biome> biome) {
      return biome.is(BiomeTags.IS_OCEAN) ? OilDepositPlan.SurfacePlacement.OCEAN : OilDepositPlan.SurfacePlacement.FLAT_LAND;
   }

   /** Chunk-center anchor for a new deposit; null when the column cannot host surface oil. */
   @Nullable
   public static BlockPos resolveDepositAnchor(WorldGenLevel level, Holder<Biome> biome, int anchorX, int anchorZ) {
      if (biome.is(BiomeTags.IS_OCEAN)) {
         BlockPos water = findOceanOilSurface(level, anchorX, anchorZ);
         return water == null ? null : new BlockPos(anchorX, water.getY(), anchorZ);
      }

      BlockPos ground = findTerrainUpper(level, anchorX, anchorZ, OilDepositPlan.SurfacePlacement.FLAT_LAND);
      return isValidLandSurfaceColumn(level, ground) ? ground : null;
   }

   /**
    * Small land deposits need a mostly flat owner chunk so surface oil does not spill down cliffs.
    * Checks the full 16×16 chunk plus the tendril footprint around the chunk center.
    */
   public static boolean isFlatLandDepositSite(WorldGenLevel level, int chunkCenterX, int chunkCenterZ, int tendrilRadius) {
      int chunkX = chunkCenterX >> 4;
      int chunkZ = chunkCenterZ >> 4;
      int baseX = chunkX << 4;
      int baseZ = chunkZ << 4;

      int minChunkY = Integer.MAX_VALUE;
      int maxChunkY = Integer.MIN_VALUE;
      int validChunk = 0;
      for (int x = baseX; x < baseX + 16; x++) {
         for (int z = baseZ; z < baseZ + 16; z++) {
            BlockPos ground = findTerrainUpper(level, x, z, OilDepositPlan.SurfacePlacement.FLAT_LAND);
            if (!isValidLandSurfaceColumn(level, ground)) {
               continue;
            }
            validChunk++;
            minChunkY = Math.min(minChunkY, ground.getY());
            maxChunkY = Math.max(maxChunkY, ground.getY());
         }
      }

      if (validChunk < LAND_FLAT_CHUNK_MIN_VALID || maxChunkY - minChunkY > LAND_FLAT_CHUNK_MAX_SPREAD) {
         return false;
      }

      int minTendrilY = Integer.MAX_VALUE;
      int maxTendrilY = Integer.MIN_VALUE;
      int validTendril = 0;
      int totalTendril = 0;
      for (int dx = -tendrilRadius; dx <= tendrilRadius; dx++) {
         for (int dz = -tendrilRadius; dz <= tendrilRadius; dz++) {
            totalTendril++;
            int x = chunkCenterX + dx;
            int z = chunkCenterZ + dz;
            BlockPos ground = findTerrainUpper(level, x, z, OilDepositPlan.SurfacePlacement.FLAT_LAND);
            if (!isValidLandSurfaceColumn(level, ground)) {
               continue;
            }
            validTendril++;
            minTendrilY = Math.min(minTendrilY, ground.getY());
            maxTendrilY = Math.max(maxTendrilY, ground.getY());
         }
      }

      return validTendril >= totalTendril * LAND_FLAT_TENDRIL_MIN_VALID
         && maxTendrilY - minTendrilY <= LAND_FLAT_TENDRIL_MAX_SPREAD;
   }

   /** Surface block to replace with oil for one tendril column. */
   @Nullable
   public static BlockPos resolvePatternColumn(
      LevelAccessor level,
      OilDepositPlan.DepositType type,
      OilDepositPlan.SurfacePlacement placement,
      int x,
      int z,
      int surfaceCenterY
   ) {
      if (placement == OilDepositPlan.SurfacePlacement.OCEAN) {
         return findOceanOilSurface(level, x, z);
      }

      BlockPos ground = findTerrainUpper(level, x, z, OilDepositPlan.SurfacePlacement.FLAT_LAND);
      if (!isValidLandSurfaceColumn(level, ground)) {
         return null;
      }

      if (type.requiresFlatLandSite()) {
         int delta = ground.getY() - surfaceCenterY;
         if (delta < -1 || delta > LAND_FLAT_TENDRIL_MAX_SPREAD) {
            return null;
         }
      }

      return ground;
   }

   /** Used by spout emergence where deposit type is unavailable. */
   @Nullable
   public static BlockPos resolveSurfaceColumn(LevelAccessor level, OilDepositPlan.SurfacePlacement placement, int x, int z) {
      if (placement == OilDepositPlan.SurfacePlacement.OCEAN) {
         return findOceanOilSurface(level, x, z);
      }

      BlockPos ground = findTerrainUpper(level, x, z, OilDepositPlan.SurfacePlacement.FLAT_LAND);
      return isValidLandSurfaceColumn(level, ground) ? ground : null;
   }

   public static int countPlaceablePatternCells(
      WorldGenLevel level,
      boolean[][] pattern,
      BlockPos patternStart,
      OilDepositPlan.DepositType type,
      OilDepositPlan.SurfacePlacement placement,
      int surfaceCenterY
   ) {
      int count = 0;
      int originX = patternStart.getX();
      int originZ = patternStart.getZ();
      for (int px = 0; px < pattern.length; px++) {
         for (int pz = 0; pz < pattern[px].length; pz++) {
            if (!pattern[px][pz]) {
               continue;
            }
            if (resolvePatternColumn(level, type, placement, originX + px, originZ + pz, surfaceCenterY) != null) {
               count++;
            }
         }
      }
      return count;
   }

   public static int minRequiredPlaceableCells(OilDepositPlan.DepositType type, int patternCells) {
      boolean small = type.tendrilSize() == OilDepositPlan.TendrilSize.SMALL;
      double ratio = small ? LAND_SMALL_MIN_PLACE_RATIO : LAND_LARGE_MIN_PLACE_RATIO;
      int floor = small ? LAND_SMALL_MIN_PLACEABLE : LAND_LARGE_MIN_PLACEABLE;
      return Math.max(floor, (int)Math.ceil(patternCells * ratio));
   }

   /**
    * BC 8.0 never preflighted ocean patterns. Land deposits reject layouts with too few valid columns
    * (cliffs, water, structures).
    */
   public static boolean passesPatternPreflight(
      WorldGenLevel level,
      boolean[][] pattern,
      BlockPos patternStart,
      OilDepositPlan.DepositType type,
      OilDepositPlan.SurfacePlacement placement,
      int surfaceCenterY
   ) {
      if (placement == OilDepositPlan.SurfacePlacement.OCEAN) {
         return true;
      }

      int patternCells = OilTendrilPattern.countCells(pattern);
      int placeable = countPlaceablePatternCells(level, pattern, patternStart, type, placement, surfaceCenterY);
      return placeable >= minRequiredPlaceableCells(type, patternCells);
   }

   /** Where a geyser tube meets the surface. */
   @Nullable
   public static BlockPos resolveSpoutEmergence(LevelAccessor level, Holder<Biome> biome, int x, int z) {
      return resolveSurfaceColumn(level, surfacePlacementFor(biome), x, z);
   }

   public static BlockPos findTerrainUpper(LevelAccessor level, int x, int z, OilDepositPlan.SurfacePlacement placement) {
      if (level instanceof WorldGenLevel wg) {
         Heightmap.Types heightmap = placement == OilDepositPlan.SurfacePlacement.OCEAN
            ? Heightmap.Types.WORLD_SURFACE
            : Heightmap.Types.MOTION_BLOCKING_NO_LEAVES;
         return new BlockPos(x, wg.getHeight(heightmap, x, z) - 1, z);
      }

      if (level instanceof LevelReader reader) {
         Heightmap.Types heightmap = placement == OilDepositPlan.SurfacePlacement.OCEAN
            ? Heightmap.Types.WORLD_SURFACE
            : Heightmap.Types.MOTION_BLOCKING_NO_LEAVES;
         return new BlockPos(x, reader.getHeight(heightmap, x, z) - 1, z);
      }

      for (int y = level.getMaxY(); y > level.getMinY(); y--) {
         BlockPos pos = new BlockPos(x, y, z);
         BlockState state = level.getBlockState(pos);
         if (!state.isAir() && BlockUtil.blocksMotion(state)) {
            return new BlockPos(x, y - 1, z);
         }
      }

      return new BlockPos(x, level.getMinY(), z);
   }

   public static int landSpreadClearHeight() {
      return LAND_SPREAD_CLEAR_HEIGHT;
   }

   public static boolean isReplaceableForLake(LevelAccessor level, BlockPos pos) {
      BlockState state = level.getBlockState(pos);
      if (state.isAir()) {
         return true;
      }

      if (BlockUtil.getFluidWithFlowing(state.getBlock()) != null) {
         return true;
      }

      if (state.canBeReplaced()) {
         return true;
      }

      if (state.is(BlockTags.SAND) || state.is(BlockTags.DIRT) || state.is(BlockTags.FLOWERS)) {
         return true;
      }

      return !BlockUtil.blocksMotion(state);
   }

   public static boolean isValidLandSurfaceColumn(LevelAccessor level, BlockPos ground) {
      if (!isReplaceableForLake(level, ground)) {
         return false;
      }
      BlockState groundState = level.getBlockState(ground);
      if (!groundState.getFluidState().isEmpty()) {
         return false;
      }
      BlockState above = level.getBlockState(ground.above());
      return above.getFluidState().isEmpty() && !above.is(Blocks.WATER);
   }

   @Nullable
   public static BlockPos findOceanOilSurface(LevelAccessor level, int x, int z) {
      int startY = readSurfaceY(level, x, z, Heightmap.Types.WORLD_SURFACE);
      for (int y = startY; y >= level.getMinY(); y--) {
         BlockPos pos = new BlockPos(x, y, z);
         BlockState state = level.getBlockState(pos);
         if (state.getFluidState().is(FluidTags.WATER)) {
            return pos;
         }

         if (isAquaticSurfacePlant(state)) {
            return pos;
         }

         if (state.is(Blocks.ICE) || state.is(Blocks.FROSTED_ICE) || state.is(Blocks.PACKED_ICE) || state.is(Blocks.SNOW_BLOCK)) {
            continue;
         }

         if (state.isAir() || state.canBeReplaced()) {
            continue;
         }

         if (!BlockUtil.blocksMotion(state)) {
            continue;
         }

         return null;
      }

      return null;
   }

   private static boolean isAquaticSurfacePlant(BlockState state) {
      return state.is(Blocks.SEAGRASS)
         || state.is(Blocks.TALL_SEAGRASS)
         || state.is(Blocks.KELP)
         || state.is(Blocks.KELP_PLANT);
   }

   private static int readSurfaceY(LevelAccessor level, int x, int z, Heightmap.Types heightmap) {
      if (level instanceof WorldGenLevel wg) {
         return wg.getHeight(heightmap, x, z) - 1;
      }
      if (level instanceof LevelReader reader) {
         return reader.getHeight(heightmap, x, z) - 1;
      }
      return level.getMaxY();
   }

   public static Box surfacePatternBounds(BlockPos start, boolean[][] pattern, int surfaceY) {
      int minY = Math.max(surfaceY - SURFACE_BOX_Y_MARGIN - OilDepositPlan.SURFACE_DEPTH, 1);
      int maxY = surfaceY + OCEAN_SPREAD_CLEAR_HEIGHT + SURFACE_BOX_Y_MARGIN;
      int depthZ = pattern.length == 0 ? 1 : pattern[0].length;
      return new Box(new BlockPos(start.getX(), minY, start.getZ()), new BlockPos(start.getX() + pattern.length - 1, maxY, start.getZ() + depthZ - 1));
   }

   public static final class Spring {
      private final BlockPos pos;

      public Spring(BlockPos pos) {
         this.pos = pos;
      }

      public void generate(LevelAccessor level, int count) {
         BlockPos placement = resolvePlacement(level, this.pos);
         if (placement == null) {
            BCLog.logger.warn("[energy.gen.oil] Skipping oil spring at " + this.pos + " because no bedrock was found.");
            return;
         }

         BlockState state = BCCoreBlocks.SPRING_OIL.defaultBlockState();
         level.setBlock(placement, state, GEN_FLAGS);
         setOil(level, placement.above());
         if (level.getBlockEntity(placement) instanceof TileSpringOil spring) {
            spring.totalSources = count;
         } else {
            BCLog.logger.warn("[energy.gen.oil] Setting the blockstate didn't also set the tile at " + placement);
         }
      }

      @Nullable
      public static BlockPos resolvePlacement(LevelAccessor level, BlockPos column) {
         if (level instanceof Level world && world.dimension() == Level.NETHER) {
            return BlockUtil.blocksMotion(level.getBlockState(column)) ? column : null;
         }

         int x = column.getX();
         int z = column.getZ();
         int scanMax = level.getMinY() + 4;

         for (int y = level.getMinY(); y <= scanMax; y++) {
            BlockPos pos = new BlockPos(x, y, z);
            if (level.getBlockState(pos).is(Blocks.BEDROCK)) {
               return pos;
            }
         }

         return null;
      }
   }
}

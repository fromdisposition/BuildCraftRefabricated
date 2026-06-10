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
import buildcraft.fabric.fluid.BcFluidUtil;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.VecUtil;
import buildcraft.lib.misc.data.Box;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public abstract class OilGenStructure {
   public final Box box;
   public final OilGenStructure.ReplaceType replaceType;
   /** BC 8.0 uses 2 — no neighbor fluid updates during worldgen. */
   private static final int GEN_FLAGS = 2;
   static final int SURFACE_BOX_Y_MARGIN = 8;
   static final int OCEAN_SPREAD_CLEAR_HEIGHT = 5;
   static final int LAND_SPREAD_CLEAR_HEIGHT = 1;

   protected final List<BlockPos> placedOilPositions = new ArrayList<>();

   public OilGenStructure(Box containingBox, OilGenStructure.ReplaceType replaceType) {
      this.box = containingBox;
      this.replaceType = replaceType;
   }

   public List<BlockPos> getPlacedOilPositions() {
      return Collections.unmodifiableList(this.placedOilPositions);
   }

   public final void generate(LevelAccessor level, Box within) {
      Box intersect = this.box.getIntersect(within);
      if (intersect != null) {
         this.generateWithin(level, intersect);
      }
   }

   protected abstract void generateWithin(LevelAccessor var1, Box var2);

   protected abstract int countOilBlocks();

   public void setOilIfCanReplace(LevelAccessor level, BlockPos pos) {
      if (this.canReplaceForOil(level, pos)) {
         this.placeOil(level, pos);
      }
   }

   protected void placeOil(LevelAccessor level, BlockPos pos) {
      setOil(level, pos);
      this.placedOilPositions.add(pos.immutable());
   }

   public boolean canReplaceForOil(LevelAccessor level, BlockPos pos) {
      return this.replaceType.canReplace(level, pos);
   }

   public static void setOil(LevelAccessor level, BlockPos pos) {
      BlockState oil = BCEnergyFluidsFabric.oilSourceBlockStateForLevel(level instanceof Level l ? l : null);
      if (oil == null) {
         oil = BCEnergyFluidsFabric.OIL_COOL.still().defaultFluidState().createLegacyBlock();
      }

      level.setBlock(pos, oil, GEN_FLAGS);
   }

   public static BlockPos findTerrainUpper(LevelAccessor level, int x, int z) {
      if (level instanceof WorldGenLevel wg) {
         return new BlockPos(x, wg.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z) - 1, z);
      }

      if (level instanceof LevelReader reader) {
         return new BlockPos(x, reader.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z) - 1, z);
      }

      for (int y = level.getMaxY(); y > level.getMinY(); y--) {
         BlockPos pos = new BlockPos(x, y, z);
         if (!level.getBlockState(pos).isAir() && BlockUtil.blocksMotion(level.getBlockState(pos))) {
            return new BlockPos(x, y - 1, z);
         }
      }

      return new BlockPos(x, level.getMinY(), z);
   }

   @Nullable
   protected static BlockPos findSurfaceWater(LevelAccessor level, int x, int z, int surfaceY) {
      for (int dy = 0; dy >= -4; dy--) {
         BlockPos pos = new BlockPos(x, surfaceY + dy, z);
         if (BcFluidUtil.isVanillaWater(level.getFluidState(pos))) {
            return pos;
         }
      }

      return null;
   }

   protected static void clearSpreadSpaceAbove(LevelAccessor level, BlockPos surface, int height) {
      for (int y = 1; y <= height; y++) {
         BlockPos above = surface.above(y);
         BlockState state = level.getBlockState(above);
         if (state.isAir() || state.canBeReplaced()) {
            level.setBlock(above, Blocks.AIR.defaultBlockState(), GEN_FLAGS);
         }
      }
   }

   private static Box createPatternBox(BlockPos start, boolean[][] pattern, int minY, int maxY) {
      BlockPos min = new BlockPos(start.getX(), minY, start.getZ());
      BlockPos max = new BlockPos(start.getX() + pattern.length - 1, maxY, start.getZ() + patternDepth(pattern) - 1);
      return new Box(min, max);
   }

   private static int patternDepth(boolean[][] pattern) {
      return pattern.length == 0 ? 1 : pattern[0].length;
   }

   private static boolean isPatternSet(boolean[][] pattern, int x, int z) {
      return x >= 0 && x < pattern.length && z >= 0 && z < pattern[x].length && pattern[x][z];
   }

   private static int countPatternCells(boolean[][] pattern) {
      int count = 0;

      for (boolean[] row : pattern) {
         for (boolean cell : row) {
            if (cell) {
               count++;
            }
         }
      }

      return count;
   }

   public static class GenByPredicate extends OilGenStructure {
      public final Predicate<BlockPos> predicate;
      private final int estimatedOilBlocks;

      public GenByPredicate(
         Box containingBox, OilGenStructure.ReplaceType replaceType, Predicate<BlockPos> predicate, int estimatedOilBlocks
      ) {
         super(containingBox, replaceType);
         this.predicate = predicate;
         this.estimatedOilBlocks = estimatedOilBlocks;
      }

      @Override
      protected void generateWithin(LevelAccessor level, Box intersect) {
         for (BlockPos pos : BlockPos.betweenClosed(intersect.min(), intersect.max())) {
            if (this.predicate.test(pos)) {
               this.setOilIfCanReplace(level, pos);
            }
         }
      }

      @Override
      protected int countOilBlocks() {
         return this.estimatedOilBlocks;
      }
   }

   public static class PatternTerrainHeight extends OilGenStructure {
      private final boolean[][] pattern;
      private final int depth;
      private final int surfaceY;

      private PatternTerrainHeight(
         Box containingBox, OilGenStructure.ReplaceType replaceType, boolean[][] pattern, int depth, int surfaceY
      ) {
         super(containingBox, replaceType);
         this.pattern = pattern;
         this.depth = depth;
         this.surfaceY = surfaceY;
      }

      public static OilGenStructure.PatternTerrainHeight create(BlockPos start, boolean[][] pattern, int depth, int surfaceY) {
         int minY = Math.max(surfaceY - SURFACE_BOX_Y_MARGIN - depth, 1);
         int maxY = surfaceY + OCEAN_SPREAD_CLEAR_HEIGHT + SURFACE_BOX_Y_MARGIN;
         Box box = createPatternBox(start, pattern, minY, maxY);
         return new OilGenStructure.PatternTerrainHeight(box, OilGenStructure.ReplaceType.ALWAYS, pattern, depth, surfaceY);
      }

      @Override
      protected void generateWithin(LevelAccessor level, Box intersect) {
         for (int x = intersect.min().getX(); x <= intersect.max().getX(); x++) {
            int px = x - this.box.min().getX();

            for (int z = intersect.min().getZ(); z <= intersect.max().getZ(); z++) {
               int pz = z - this.box.min().getZ();
               if (!isPatternSet(this.pattern, px, pz)) {
                  continue;
               }

               BlockPos surface = findSurfaceWater(level, x, z, this.surfaceY);
               if (surface != null) {
                  clearSpreadSpaceAbove(level, surface, OCEAN_SPREAD_CLEAR_HEIGHT);
                  this.setOilIfCanReplace(level, surface);
                  continue;
               }

               BlockPos upper = findTerrainUpper(level, x, z);
               if (!this.canReplaceForOil(level, upper)) {
                  continue;
               }

               for (int y = 0; y < LAND_SPREAD_CLEAR_HEIGHT; y++) {
                  level.setBlock(upper.above(y), Blocks.AIR.defaultBlockState(), GEN_FLAGS);
               }

               this.setOilIfCanReplace(level, upper);
               for (int y = 1; y < this.depth; y++) {
                  this.setOilIfCanReplace(level, upper.below(y));
               }
            }
         }
      }

      @Override
      protected int countOilBlocks() {
         return countPatternCells(this.pattern) * this.depth;
      }
   }

   public enum ReplaceType {
      ALWAYS {
         @Override
         public boolean canReplace(LevelAccessor level, BlockPos pos) {
            return true;
         }
      };

      public abstract boolean canReplace(LevelAccessor var1, BlockPos var2);
   }

   public static class Spout extends OilGenStructure {
      public final BlockPos start;
      public final int radius;
      public final int height;
      private int count = 0;

      public Spout(BlockPos start, OilGenStructure.ReplaceType replaceType, int radius, int height) {
         super(createBox(start, height, radius), replaceType);
         this.start = start;
         this.radius = radius;
         this.height = height;
      }

      public boolean hasPlacedOil() {
         return this.count > 0;
      }

      private static Box createBox(BlockPos start, int height, int radius) {
         int top = start.getY() + height * (radius + 2) + 128;
         return new Box(start, new BlockPos(start.getX(), top, start.getZ()));
      }

      @Override
      protected void generateWithin(LevelAccessor level, Box intersect) {
         this.count = 0;
         int maxY = level.getMaxY();
         BlockPos worldTop = new BlockPos(this.start.getX(), maxY, this.start.getZ());

         for (int y = maxY; y >= this.start.getY(); y--) {
            worldTop = new BlockPos(this.start.getX(), y, this.start.getZ());
            BlockState state = level.getBlockState(worldTop);
            if (!state.isAir() && (BlockUtil.getFluidWithFlowing(state.getBlock()) != null || BlockUtil.blocksMotion(state))) {
               break;
            }
         }

         int tubeLen = Math.max(0, worldTop.getY() - this.start.getY());
         OilGenStructure tubeY = OilGenerator.createTube(this.start, tubeLen, this.radius, Axis.Y);
         this.generateChild(level, intersect, tubeY);
         this.count += tubeY.countOilBlocks();
         BlockPos base = worldTop;

         for (int r = this.radius; r >= 0; r--) {
            OilGenStructure struct = OilGenerator.createTube(base, this.height, r, Axis.Y);
            this.generateChild(level, intersect, struct);
            this.count += struct.countOilBlocks();
            base = base.offset(0, this.height, 0);
         }
      }

      private void generateChild(LevelAccessor level, Box intersect, OilGenStructure child) {
         child.generate(level, intersect);
         this.placedOilPositions.addAll(child.getPlacedOilPositions());
      }

      @Override
      protected int countOilBlocks() {
         return this.count;
      }
   }

   public static class Spring extends OilGenStructure {
      public final BlockPos pos;

      public Spring(BlockPos pos) {
         super(new Box(pos, pos), OilGenStructure.ReplaceType.ALWAYS);
         this.pos = pos;
      }

      @Override
      protected void generateWithin(LevelAccessor level, Box intersect) {
      }

      @Override
      protected int countOilBlocks() {
         return 0;
      }

      public void generate(LevelAccessor level, int count) {
         BlockPos placement = resolvePlacement(level, this.pos);
         if (placement == null) {
            BCLog.logger.warn("[energy.gen.oil] Skipping oil spring at " + this.pos + " because no bedrock was found.");
            return;
         }

         BlockState state = BCCoreBlocks.SPRING_OIL.defaultBlockState();
         level.setBlock(placement, state, GEN_FLAGS);
         this.placeOil(level, placement.above());
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

   public static class SurfacePool extends OilGenStructure {
      private final boolean[][] pattern;
      private final int depth;

      private SurfacePool(Box containingBox, OilGenStructure.ReplaceType replaceType, boolean[][] pattern, int depth) {
         super(containingBox, replaceType);
         this.pattern = pattern;
         this.depth = depth;
      }

      public static OilGenStructure.SurfacePool create(BlockPos start, boolean[][] pattern, int depth, int surfaceY) {
         int minY = Math.max(surfaceY - SURFACE_BOX_Y_MARGIN - depth, 1);
         int maxY = surfaceY + LAND_SPREAD_CLEAR_HEIGHT + SURFACE_BOX_Y_MARGIN;
         Box box = createPatternBox(start, pattern, minY, maxY);
         return new OilGenStructure.SurfacePool(box, OilGenStructure.ReplaceType.ALWAYS, pattern, depth);
      }

      @Override
      protected void generateWithin(LevelAccessor level, Box intersect) {
         for (int x = intersect.min().getX(); x <= intersect.max().getX(); x++) {
            int px = x - this.box.min().getX();

            for (int z = intersect.min().getZ(); z <= intersect.max().getZ(); z++) {
               int pz = z - this.box.min().getZ();
               if (!isPatternSet(this.pattern, px, pz)) {
                  continue;
               }

               BlockPos upper = findTerrainUpper(level, x, z);
               if (!this.canReplaceForOil(level, upper)) {
                  continue;
               }

               for (int y = 0; y < LAND_SPREAD_CLEAR_HEIGHT; y++) {
                  level.setBlock(upper.above(y), Blocks.AIR.defaultBlockState(), GEN_FLAGS);
               }

               this.setOilIfCanReplace(level, upper);
               for (int y = 1; y < this.depth; y++) {
                  this.setOilIfCanReplace(level, upper.below(y));
               }
            }
         }
      }

      @Override
      protected int countOilBlocks() {
         return countPatternCells(this.pattern) * this.depth;
      }
   }
}

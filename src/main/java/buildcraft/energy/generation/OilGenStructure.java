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
import buildcraft.lib.misc.VecUtil;
import buildcraft.lib.misc.data.Box;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public abstract class OilGenStructure {
   public final Box box;
   public final OilGenStructure.ReplaceType replaceType;
   private static final int MAX_TREE_SCAN_HEIGHT = 32;
   private static final int TREE_CLEAR_CHUNK_EXPANSION = 4;
   private static final int TREE_CLEAR_BFS_BUDGET = 8192;
   /** Neighbor + client updates so fluids schedule ticks during worldgen (same as water SpringWorldgen). */
   private static final int GEN_FLAGS = 3;

   public OilGenStructure(Box containingBox, OilGenStructure.ReplaceType replaceType) {
      this.box = containingBox;
      this.replaceType = replaceType;
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
         setOil(level, pos);
      }
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
      scheduleOilFluidTick(level, pos, oil);
   }

   private static void scheduleOilFluidTick(LevelAccessor level, BlockPos pos, BlockState oil) {
      if (level instanceof WorldGenLevel worldGen) {
         FluidState fluidState = oil.getFluidState();
         if (!fluidState.isEmpty()) {
            worldGen.scheduleTick(pos, fluidState.getType(), 0);
         }
      }
   }

   protected static BlockPos findSolidSurfaceTop(LevelAccessor level, int x, int z) {
      int maxY = level.getMaxY();
      int minY = level.getMinY();

      for (int y = maxY; y > minY; y--) {
         BlockPos p = new BlockPos(x, y, z);
         BlockState state = level.getBlockState(p);
         if (!state.isAir() && !state.canBeReplaced()) {
            return p;
         }
      }

      return new BlockPos(x, minY, z);
   }

   @Nullable
   protected static BlockPos findTopWater(LevelAccessor level, int x, int z) {
      for (int y = level.getMaxY(); y > level.getMinY(); y--) {
         BlockPos pos = new BlockPos(x, y, z);
         if (level.getFluidState(pos).is(FluidTags.WATER)) {
            return pos;
         }
      }

      return null;
   }

   protected static boolean canPlaceOilFilmAt(LevelAccessor level, BlockPos pos) {
      if (pos.getY() > level.getMaxY()) {
         return false;
      }

      BlockState state = level.getBlockState(pos);
      return state.isAir() || state.canBeReplaced();
   }

   /** Air block directly above the top water cell — oil genesis for surface film spread. */
   @Nullable
   protected static BlockPos findOilFilmPos(LevelAccessor level, BlockPos topWater) {
      BlockPos film = topWater.above();
      return canPlaceOilFilmAt(level, film) ? film : null;
   }

   protected static boolean isReplaceableForLake(LevelAccessor level, BlockPos pos) {
      BlockState state = level.getBlockState(pos);
      if (state.isAir()) {
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

   protected static BlockPos findTerrainUpper(LevelAccessor level, int x, int z) {
      if (level instanceof LevelReader reader) {
         return new BlockPos(x, reader.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z) - 1, z);
      }

      BlockPos top = findSolidSurfaceTop(level, x, z);
      return top.below();
   }

   /** BC 8.0 PatternTerrainHeight: one source on water surface film, or depth blocks in terrain on land. */
   protected static void placeLakeOilPatch(LevelAccessor level, int x, int z, int depth) {
      BlockPos topWater = findTopWater(level, x, z);
      if (topWater != null) {
         BlockPos film = findOilFilmPos(level, topWater);
         if (film != null) {
            setOil(level, film);
         }

         return;
      }

      BlockPos upper = findTerrainUpper(level, x, z);
      if (!isReplaceableForLake(level, upper)) {
         return;
      }

      for (int y = 0; y < 5; y++) {
         BlockPos clear = upper.above(y);
         BlockState clearState = level.getBlockState(clear);
         if (clearState.isAir() || clearState.canBeReplaced()) {
            level.setBlock(clear, Blocks.AIR.defaultBlockState(), GEN_FLAGS);
         }
      }

      for (int y = 0; y < depth; y++) {
         BlockPos place = upper.below(y);
         if (isReplaceableForLake(level, place)) {
            setOil(level, place);
         }
      }
   }

   protected static BlockPos clearTreesAndFindGround(LevelAccessor level, BlockPos baseTop, Box chunkBox) {
      Set<Long> visited = new HashSet<>();
      BlockState baseState = level.getBlockState(baseTop);
      if (!baseState.is(BlockTags.LOGS) && !baseState.is(BlockTags.LEAVES)) {
         for (int dy = 1; dy <= MAX_TREE_SCAN_HEIGHT; dy++) {
            BlockPos pos = baseTop.above(dy);
            BlockState s = level.getBlockState(pos);
            if (s.is(BlockTags.LOGS) || s.is(BlockTags.LEAVES)) {
               bfsClearTree(level, pos, chunkBox, visited);
               break;
            }

            if (!s.isAir() && !s.canBeReplaced()) {
               break;
            }
         }
      } else {
         bfsClearTree(level, baseTop, chunkBox, visited);
      }

      BlockPos pos = baseTop;
      int minY = level.getMinY();

      for (int i = 0; i < 64; i++) {
         if (pos.getY() <= minY) {
            return pos;
         }

         BlockState state = level.getBlockState(pos);
         if (!state.is(BlockTags.LOGS) && !state.is(BlockTags.LEAVES)) {
            if (!state.isAir() && !state.canBeReplaced()) {
               return pos;
            }
         } else {
            bfsClearTree(level, pos, chunkBox, visited);
         }

         pos = pos.below();
      }

      return pos;
   }

   private static void bfsClearTree(LevelAccessor level, BlockPos start, Box chunkBox, Set<Long> visited) {
      int minX = chunkBox.min().getX() - TREE_CLEAR_CHUNK_EXPANSION;
      int maxX = chunkBox.max().getX() + TREE_CLEAR_CHUNK_EXPANSION;
      int minZ = chunkBox.min().getZ() - TREE_CLEAR_CHUNK_EXPANSION;
      int maxZ = chunkBox.max().getZ() + TREE_CLEAR_CHUNK_EXPANSION;
      if (visited.add(start.asLong())) {
         ArrayDeque<BlockPos> queue = new ArrayDeque<>();
         queue.add(start);
         int budget = TREE_CLEAR_BFS_BUDGET;

         while (!queue.isEmpty() && budget-- > 0) {
            BlockPos pos = queue.poll();
            BlockState state = level.getBlockState(pos);
            if (state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES)) {
               level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);

               for (int dx = -1; dx <= 1; dx++) {
                  for (int dy = -1; dy <= 1; dy++) {
                     for (int dz = -1; dz <= 1; dz++) {
                        if (dx != 0 || dy != 0 || dz != 0) {
                           BlockPos n = pos.offset(dx, dy, dz);
                           if (n.getX() >= minX && n.getX() <= maxX && n.getZ() >= minZ && n.getZ() <= maxZ && visited.add(n.asLong())) {
                              BlockState ns = level.getBlockState(n);
                              if (ns.is(BlockTags.LOGS) || ns.is(BlockTags.LEAVES)) {
                                 queue.add(n);
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public static class GenByPredicate extends OilGenStructure {
      public final Predicate<BlockPos> predicate;

      public GenByPredicate(Box containingBox, OilGenStructure.ReplaceType replaceType, Predicate<BlockPos> predicate) {
         super(containingBox, replaceType);
         this.predicate = predicate;
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
         int count = 0;

         for (BlockPos pos : BlockPos.betweenClosed(this.box.min(), this.box.max())) {
            if (this.predicate.test(pos)) {
               count++;
            }
         }

         return count;
      }
   }

   public static class PatternTerrainHeight extends OilGenStructure {
      private final boolean[][] pattern;
      private final int depth;

      private PatternTerrainHeight(Box containingBox, OilGenStructure.ReplaceType replaceType, boolean[][] pattern, int depth) {
         super(containingBox, replaceType);
         this.pattern = pattern;
         this.depth = depth;
      }

      public static OilGenStructure.PatternTerrainHeight create(
         BlockPos start, OilGenStructure.ReplaceType replaceType, boolean[][] pattern, int depth, int maxY
      ) {
         BlockPos min = VecUtil.replaceValue(start, Axis.Y, 1);
         BlockPos max = min.offset(pattern.length - 1, maxY, pattern.length == 0 ? 0 : pattern[0].length - 1);
         Box box = new Box(min, max);
         return new OilGenStructure.PatternTerrainHeight(box, replaceType, pattern, depth);
      }

      @Override
      protected void generateWithin(LevelAccessor level, Box intersect) {
         for (int x = intersect.min().getX(); x <= intersect.max().getX(); x++) {
            int px = x - this.box.min().getX();

            for (int z = intersect.min().getZ(); z <= intersect.max().getZ(); z++) {
               int pz = z - this.box.min().getZ();
               if (this.pattern[px][pz]) {
                  placeLakeOilPatch(level, x, z, this.depth);
               }
            }
         }
      }

      @Override
      protected int countOilBlocks() {
         int count = 0;

         for (int x = 0; x < this.pattern.length; x++) {
            for (int z = 0; z < this.pattern[x].length; z++) {
               if (this.pattern[x][z]) {
                  count++;
               }
            }
         }

         return count * this.depth;
      }
   }

   public enum ReplaceType {
      ALWAYS {
         @Override
         public boolean canReplace(LevelAccessor level, BlockPos pos) {
            return true;
         }
      },
      IS_FOR_LAKE {
         @Override
         public boolean canReplace(LevelAccessor level, BlockPos pos) {
            return isReplaceableForLake(level, pos);
         }
      },
      SKIP_WATER {
         @Override
         public boolean canReplace(LevelAccessor level, BlockPos pos) {
            FluidState fluid = level.getFluidState(pos);
            return !fluid.is(FluidTags.WATER) && !fluid.is(FluidTags.LAVA);
         }
      };

      public abstract boolean canReplace(LevelAccessor var1, BlockPos var2);
   }

   public static class Spout extends OilGenStructure {
      public final BlockPos start;
      public final int radius;
      public final int height;
      private int count = 0;

      public Spout(BlockPos start, OilGenStructure.ReplaceType replaceType, int radius, int height, int maxY) {
         super(createBox(start, maxY), replaceType);
         this.start = start;
         this.radius = radius;
         this.height = height;
      }

      public boolean hasPlacedOil() {
         return this.count > 0;
      }

      private static Box createBox(BlockPos start, int maxY) {
         return new Box(start, VecUtil.replaceValue(start, Axis.Y, maxY));
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

         OilGenStructure tubeY = OilGenerator.createTube(
            this.start, worldTop.getY() - this.start.getY(), this.radius, Axis.Y, OilGenStructure.ReplaceType.SKIP_WATER
         );
         tubeY.generate(level, tubeY.box);
         this.count = this.count + tubeY.countOilBlocks();
         BlockPos base = worldTop;

         for (int r = this.radius; r >= 0; r--) {
            OilGenStructure struct = OilGenerator.createTube(base, this.height, r, Axis.Y);
            struct.generate(level, struct.box);
            base = base.offset(0, this.height, 0);
            this.count = this.count + struct.countOilBlocks();
         }
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

   public static class SurfacePool extends OilGenStructure {
      private final boolean[][] pattern;
      private final int depth;

      private SurfacePool(Box containingBox, OilGenStructure.ReplaceType replaceType, boolean[][] pattern, int depth) {
         super(containingBox, replaceType);
         this.pattern = pattern;
         this.depth = depth;
      }

      public static OilGenStructure.SurfacePool create(
         BlockPos start, OilGenStructure.ReplaceType replaceType, boolean[][] pattern, int depth, int maxY
      ) {
         BlockPos min = VecUtil.replaceValue(start, Axis.Y, 1);
         BlockPos max = min.offset(pattern.length - 1, maxY, pattern.length == 0 ? 0 : pattern[0].length - 1);
         Box box = new Box(min, max);
         return new OilGenStructure.SurfacePool(box, replaceType, pattern, depth);
      }

      @Override
      protected void generateWithin(LevelAccessor level, Box intersect) {
         for (int x = intersect.min().getX(); x <= intersect.max().getX(); x++) {
            int px = x - this.box.min().getX();

            for (int z = intersect.min().getZ(); z <= intersect.max().getZ(); z++) {
               int pz = z - this.box.min().getZ();
               if (this.pattern[px][pz]) {
                  BlockPos baseTop = findSolidSurfaceTop(level, x, z);
                  BlockPos upper = clearTreesAndFindGround(level, baseTop, intersect);
                  if (this.canReplaceForOil(level, upper)) {
                     for (int y = 0; y < 5; y++) {
                        level.setBlock(upper.above(y), Blocks.AIR.defaultBlockState(), 2);
                     }

                     for (int y = 0; y < this.depth; y++) {
                        this.setOilIfCanReplace(level, upper.below(y));
                     }
                  }
               }
            }
         }
      }

      @Override
      protected int countOilBlocks() {
         int count = 0;

         for (int x = 0; x < this.pattern.length; x++) {
            for (int z = 0; z < this.pattern[x].length; z++) {
               if (this.pattern[x][z]) {
                  count++;
               }
            }
         }

         return count * this.depth;
      }
   }
}

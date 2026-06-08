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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public abstract class OilGenStructure {
   public final Box box;
   public final OilGenStructure.ReplaceType replaceType;
   private static final int MAX_TREE_SCAN_HEIGHT = 32;
   private static final int TREE_CLEAR_CHUNK_EXPANSION = 4;
   private static final int TREE_CLEAR_BFS_BUDGET = 8192;

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

      level.setBlock(pos, oil, 2);
   }

   protected static BlockPos findWorldSurfaceTop(LevelAccessor level, int x, int z) {
      int maxY = level.getMaxY();
      int minY = level.getMinY();

      for (int y = maxY; y > minY; y--) {
         BlockPos p = new BlockPos(x, y, z);
         if (!level.getBlockState(p).isAir()) {
            return p;
         }
      }

      return new BlockPos(x, minY, z);
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

   protected static BlockPos clearTreesAndFindGround(LevelAccessor level, BlockPos baseTop, Box chunkBox) {
      Set<Long> visited = new HashSet<>();
      BlockState baseState = level.getBlockState(baseTop);
      if (!baseState.is(BlockTags.LOGS) && !baseState.is(BlockTags.LEAVES)) {
         for (int dy = 1; dy <= 32; dy++) {
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
      int minX = chunkBox.min().getX() - 4;
      int maxX = chunkBox.max().getX() + 4;
      int minZ = chunkBox.min().getZ() - 4;
      int maxZ = chunkBox.max().getZ() + 4;
      if (visited.add(start.asLong())) {
         ArrayDeque<BlockPos> queue = new ArrayDeque<>();
         queue.add(start);
         int budget = 8192;

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

   public static class FlatPattern extends OilGenStructure {
      private final boolean[][] pattern;
      private final int depth;

      private FlatPattern(Box containingBox, OilGenStructure.ReplaceType replaceType, boolean[][] pattern, int depth) {
         super(containingBox, replaceType);
         this.pattern = pattern;
         this.depth = depth;
      }

      public static OilGenStructure.FlatPattern create(BlockPos start, OilGenStructure.ReplaceType replaceType, boolean[][] pattern, int depth) {
         BlockPos min = start.offset(0, 1 - depth, 0);
         BlockPos max = start.offset(pattern.length - 1, 0, pattern.length == 0 ? 0 : pattern[0].length - 1);
         Box box = new Box(min, max);
         return new OilGenStructure.FlatPattern(box, replaceType, pattern, depth);
      }

      @Override
      protected void generateWithin(LevelAccessor level, Box intersect) {
         BlockPos start = this.box.min();

         for (BlockPos pos : BlockPos.betweenClosed(intersect.min(), intersect.max())) {
            int x = pos.getX() - start.getX();
            int z = pos.getZ() - start.getZ();
            if (this.pattern[x][z]) {
               this.setOilIfCanReplace(level, pos);
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

      public static OilGenStructure.PatternTerrainHeight create(BlockPos start, OilGenStructure.ReplaceType replaceType, boolean[][] pattern, int depth) {
         BlockPos min = VecUtil.replaceValue(start, Axis.Y, 1);
         BlockPos max = min.offset(pattern.length - 1, 319, pattern.length == 0 ? 0 : pattern[0].length - 1);
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
                  BlockPos upper = findWorldSurfaceTop(level, x, z);
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

   public enum ReplaceType {
      ALWAYS {
         @Override
         public boolean canReplace(LevelAccessor level, BlockPos pos) {
            return true;
         }
      },
      NOT_BEDROCK {
         @Override
         public boolean canReplace(LevelAccessor level, BlockPos pos) {
            return !level.getBlockState(pos).is(Blocks.BEDROCK);
         }
      },
      IS_FOR_LAKE {
         @Override
         public boolean canReplace(LevelAccessor level, BlockPos pos) {
            return ALWAYS.canReplace(level, pos);
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
         super(createBox(start), replaceType);
         this.start = start;
         this.radius = radius;
         this.height = height;
      }

      private static Box createBox(BlockPos start) {
         return new Box(start, VecUtil.replaceValue(start, Axis.Y, 320));
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

         OilGenStructure tubeY = OilGenerator.createTube(this.start, worldTop.getY() - this.start.getY(), this.radius, Axis.Y);
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
         if (this.count == 0) {
            throw new IllegalStateException("Called countOilBlocks before calling generateWithin!");
         } else {
            return this.count;
         }
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
         BlockState state = BCCoreBlocks.SPRING_OIL.defaultBlockState();
         level.setBlock(this.pos, state, 2);
         setOil(level, this.pos.above());
         if (level.getBlockEntity(this.pos) instanceof TileSpringOil spring) {
            spring.totalSources = count;
         } else {
            BCLog.logger.warn("[energy.gen.oil] Setting the blockstate didn't also set the tile at " + this.pos);
         }
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

      public static OilGenStructure.SurfacePool create(BlockPos start, OilGenStructure.ReplaceType replaceType, boolean[][] pattern, int depth) {
         BlockPos min = VecUtil.replaceValue(start, Axis.Y, 1);
         BlockPos max = min.offset(pattern.length - 1, 319, pattern.length == 0 ? 0 : pattern[0].length - 1);
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

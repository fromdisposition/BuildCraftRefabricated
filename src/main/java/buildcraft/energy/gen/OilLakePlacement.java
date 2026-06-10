package buildcraft.energy.gen;

import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.data.Box;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

/**
 * BC 8.0 lake and surface-pool column placement ported from OilGenStructure.
 */
public final class OilLakePlacement {
   private static final int MAX_TREE_SCAN_HEIGHT = 32;
   private static final int TREE_CLEAR_CHUNK_EXPANSION = 4;
   private static final int TREE_CLEAR_BFS_BUDGET = 8192;

   private OilLakePlacement() {
   }

   public static int placeLakeColumn(LevelAccessor level, int x, int z, int depth, BlockState oil) {
      BlockPos surface = findLakePlacementSurface(level, x, z);
      if (!isReplaceableForLake(level, surface.above())) {
         return 0;
      }

      if (surface.getY() + 2 < level.getMaxY() && !level.getBlockState(surface.above(2)).isAir()) {
         return 0;
      }

      if (!isReplaceableLakeFluid(level, surface) && !hasSolidSupportBelow(level, surface)) {
         return 0;
      }

      return fillLakeColumn(level, surface, depth, oil);
   }

   public static int placeSurfacePoolColumn(LevelAccessor level, int x, int z, int depth, BlockState oil, Box bounds) {
      BlockPos baseTop = findSolidSurfaceTop(level, x, z);
      BlockPos upper = clearTreesAndFindGround(level, baseTop, bounds);
      if (!isReplaceableForLake(level, upper)) {
         return 0;
      }

      for (int y = 0; y < 5; y++) {
         level.setBlock(upper.above(y), Blocks.AIR.defaultBlockState(), 2);
      }

      int placed = 0;
      for (int y = 0; y < depth; y++) {
         BlockPos pos = upper.below(y);
         if (isReplaceableForLake(level, pos)) {
            level.setBlock(pos, oil, 2);
            placed++;
         }
      }

      return placed;
   }

   private static int fillLakeColumn(LevelAccessor level, BlockPos surface, int depth, BlockState oil) {
      level.setBlock(surface, oil, 2);
      if (!level.getBlockState(surface.above()).isAir()) {
         level.setBlock(surface.above(), Blocks.AIR.defaultBlockState(), 2);
      }

      int placed = 1;
      for (int d = 1; d < depth; d++) {
         BlockPos below = surface.below(d);
         if (!isReplaceableLakeFluid(level, below) && !hasSolidSupportBelow(level, below)) {
            break;
         }

         level.setBlock(below, oil, 2);
         placed++;
      }

      return placed;
   }

   private static BlockPos findSolidSurfaceTop(LevelAccessor level, int x, int z) {
      for (int y = level.getMaxY(); y > level.getMinY(); y--) {
         BlockPos pos = new BlockPos(x, y, z);
         BlockState state = level.getBlockState(pos);
         if (!state.isAir() && !state.canBeReplaced()) {
            return pos;
         }
      }

      return new BlockPos(x, level.getMinY(), z);
   }

   private static BlockPos findLakePlacementSurface(LevelAccessor level, int x, int z) {
      for (int y = level.getMaxY(); y > level.getMinY(); y--) {
         BlockPos pos = new BlockPos(x, y, z);
         BlockState state = level.getBlockState(pos);
         if (state.isAir()) {
            continue;
         }

         FluidState fluidState = level.getFluidState(pos);
         if (!fluidState.isEmpty() && !fluidState.getType().isSame(Fluids.LAVA)) {
            return pos;
         }

         if (!state.canBeReplaced() && BlockUtil.blocksMotion(state)) {
            if (state.is(BlockTags.FLOWERS)) {
               continue;
            }

            return pos.below();
         }
      }

      return new BlockPos(x, level.getMinY(), z);
   }

   private static boolean isReplaceableLakeFluid(LevelAccessor level, BlockPos pos) {
      FluidState fluidState = level.getFluidState(pos);
      return !fluidState.isEmpty() && !fluidState.getType().isSame(Fluids.LAVA);
   }

   private static boolean isReplaceableForLake(LevelAccessor level, BlockPos pos) {
      BlockState state = level.getBlockState(pos);
      if (state.isAir()) {
         return true;
      }

      if (isReplaceableLakeFluid(level, pos)) {
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

   private static boolean hasSolidSupportBelow(LevelAccessor level, BlockPos pos) {
      return BlockUtil.blocksMotion(level.getBlockState(pos.below()));
   }

   private static BlockPos clearTreesAndFindGround(LevelAccessor level, BlockPos baseTop, Box bounds) {
      Set<Long> visited = new HashSet<>();
      BlockState baseState = level.getBlockState(baseTop);
      if (!baseState.is(BlockTags.LOGS) && !baseState.is(BlockTags.LEAVES)) {
         for (int dy = 1; dy <= MAX_TREE_SCAN_HEIGHT; dy++) {
            BlockPos pos = baseTop.above(dy);
            BlockState state = level.getBlockState(pos);
            if (state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES)) {
               bfsClearTree(level, pos, bounds, visited);
               break;
            }

            if (!state.isAir() && !state.canBeReplaced()) {
               break;
            }
         }
      } else {
         bfsClearTree(level, baseTop, bounds, visited);
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
            bfsClearTree(level, pos, bounds, visited);
         }

         pos = pos.below();
      }

      return pos;
   }

   private static void bfsClearTree(LevelAccessor level, BlockPos start, Box bounds, Set<Long> visited) {
      int minX = bounds.min().getX() - TREE_CLEAR_CHUNK_EXPANSION;
      int maxX = bounds.max().getX() + TREE_CLEAR_CHUNK_EXPANSION;
      int minZ = bounds.min().getZ() - TREE_CLEAR_CHUNK_EXPANSION;
      int maxZ = bounds.max().getZ() + TREE_CLEAR_CHUNK_EXPANSION;
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
                           BlockPos neighbor = pos.offset(dx, dy, dz);
                           if (neighbor.getX() >= minX
                              && neighbor.getX() <= maxX
                              && neighbor.getZ() >= minZ
                              && neighbor.getZ() <= maxZ
                              && visited.add(neighbor.asLong())) {
                              BlockState neighborState = level.getBlockState(neighbor);
                              if (neighborState.is(BlockTags.LOGS) || neighborState.is(BlockTags.LEAVES)) {
                                 queue.add(neighbor);
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
}

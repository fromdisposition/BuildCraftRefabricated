package buildcraft.core.gen;

import buildcraft.core.BCCoreBlocks;
import buildcraft.lib.misc.BlockUtil;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * BC 8.0 spring column geometry shared by water and oil worldgen features.
 */
public final class SpringWorldgen {
   private SpringWorldgen() {
   }

   public static boolean placeWaterSpring(WorldGenLevel level, int x, int z) {
      BlockPos bedrock = SpringPlacement.findBedrock(level, x, z);
      if (bedrock == null) {
         return false;
      }

      level.setBlock(bedrock, BCCoreBlocks.SPRING_WATER.defaultBlockState(), 3);

      for (int y = bedrock.getY() + 2; y < level.getMaxY(); y++) {
         BlockPos column = new BlockPos(x, y, z);
         if (level.isEmptyBlock(column)) {
            break;
         }

         level.setBlock(column, Blocks.WATER.defaultBlockState(), 3);
      }

      return true;
   }

   @Nullable
   public static BlockPos placeOilSpringOnBedrock(WorldGenLevel level, int x, int z, BlockState oil) {
      BlockPos bedrock = SpringPlacement.findBedrock(level, x, z);
      if (bedrock == null) {
         return null;
      }

      level.setBlock(bedrock, BCCoreBlocks.SPRING_OIL.defaultBlockState(), 3);
      level.setBlock(bedrock.above(), oil, 3);
      return bedrock;
   }

   public static int placeOilSpout(
      WorldGenLevel level,
      int x,
      int z,
      int wellY,
      BlockState oil,
      int stackHeight,
      int maxRadius,
      @Nullable BlockPos springPos,
      int springTubeRadius
   ) {
      int placed = 0;

      if (springPos != null && springTubeRadius > 0) {
         int tubeStart = level.getMinY() + 2;
         if (tubeStart < wellY) {
            placed += fillVerticalColumn(level, x, z, tubeStart, wellY, oil, springTubeRadius);
         }
      }

      int worldTopY = findSpoutWorldTop(level, x, z, wellY);
      placed += fillVerticalColumn(level, x, z, wellY, worldTopY, oil, maxRadius);

      int stackBaseY = worldTopY;
      for (int radius = maxRadius; radius >= 0; radius--) {
         placed += fillVerticalTube(level, x, z, stackBaseY, stackHeight, radius, oil);
         stackBaseY += stackHeight;
      }

      return placed;
   }

   public static int findSpoutWorldTop(WorldGenLevel level, int x, int z, int minY) {
      for (int y = level.getMaxY(); y >= minY; y--) {
         BlockPos pos = new BlockPos(x, y, z);
         BlockState state = level.getBlockState(pos);
         if (!state.isAir() && (BlockUtil.getFluidWithFlowing(state.getBlock()) != null || BlockUtil.blocksMotion(state))) {
            return y;
         }
      }

      return minY;
   }

   private static int fillVerticalColumn(WorldGenLevel level, int x, int z, int fromY, int toY, BlockState oil, int radius) {
      if (fromY > toY) {
         return 0;
      }

      return fillVerticalTube(level, x, z, fromY, toY - fromY + 1, radius, oil);
   }

   private static int fillVerticalTube(WorldGenLevel level, int x, int z, int startY, int height, int radius, BlockState oil) {
      int placed = 0;
      double radiusSq = radius * radius + 0.01;

      for (int dy = 0; dy < height; dy++) {
         int y = startY + dy;

         for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
               if (dx * dx + dz * dz <= radiusSq) {
                  level.setBlock(new BlockPos(x + dx, y, z + dz), oil, 3);
                  placed++;
               }
            }
         }
      }

      return placed;
   }
}

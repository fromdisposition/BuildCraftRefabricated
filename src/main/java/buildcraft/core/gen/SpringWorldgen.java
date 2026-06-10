package buildcraft.core.gen;

import buildcraft.core.BCCoreBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;

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
}

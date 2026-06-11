package buildcraft.core.gen;

import buildcraft.core.BCCoreBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.WorldGenLevel;

public final class SpringWorldgen {
   private SpringWorldgen() {
   }

   public static boolean placeWaterSpring(WorldGenLevel level, int x, int z) {
      BlockPos bedrock = SpringPlacement.findBedrock(level, x, z);
      if (bedrock == null) {
         return false;
      }

      level.setBlock(bedrock, BCCoreBlocks.SPRING_WATER.defaultBlockState(), 2);
      for (int y = bedrock.getY() + 2; y < level.getMaxY(); y++) {
         BlockPos at = new BlockPos(x, y, z);
         if (level.getBlockState(at).isAir()) {
            break;
         }
         level.setBlock(at, Blocks.WATER.defaultBlockState(), 2);
      }
      return true;
   }
}

package buildcraft.core.gen;

import buildcraft.core.BCCoreBlocks;
import net.minecraft.core.BlockPos;
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
      return true;
   }
}

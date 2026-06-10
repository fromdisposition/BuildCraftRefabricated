package buildcraft.core.gen;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;

public final class SpringPlacement {
   private SpringPlacement() {
   }

   @Nullable
   public static BlockPos findBedrock(LevelAccessor level, int x, int z) {
      int minY = level.getMinY();

      for (int y = minY; y <= minY + 4; y++) {
         BlockPos pos = new BlockPos(x, y, z);
         if (level.getBlockState(pos).is(Blocks.BEDROCK)) {
            return pos;
         }
      }

      return null;
   }
}

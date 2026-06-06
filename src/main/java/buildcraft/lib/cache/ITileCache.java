package buildcraft.lib.cache;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public interface ITileCache {
   void invalidate();

   @Nullable
   TileCacheRet getTile(BlockPos var1);

   @Nullable
   TileCacheRet getTile(Direction var1);

   enum TileCacheState {
      CACHED,
      NOT_CACHED,
      NOT_PRESENT;
   }
}

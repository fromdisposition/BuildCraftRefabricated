package buildcraft.lib.platform.menu;

import buildcraft.api.core.BCLog;
import buildcraft.lib.platform.BcRegistryUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.Nullable;

public final class MenuBlockEntityLookup {
   private MenuBlockEntityLookup() {
   }

   public static <T extends BlockEntity> @Nullable T get(Inventory playerInv, BlockPos pos, Class<T> type) {
      if (pos == null) {
         BCLog.logger.warn("[menu.lookup] Missing BlockPos for {}", type.getSimpleName());
         return null;
      } else if (playerInv.player.level() == null) {
         BCLog.logger.warn("[menu.lookup] Missing level for {} at {}", type.getSimpleName(), pos);
         return null;
      } else if (!BcRegistryUtil.isChunkLoaded(playerInv.player.level(), pos)) {
         BCLog.logger.warn("[menu.lookup] Chunk not loaded for {} at {}", type.getSimpleName(), pos);
         return null;
      } else {
         BlockEntity be = playerInv.player.level().getBlockEntity(pos);
         if (!type.isInstance(be)) {
            BCLog.logger.warn("[menu.lookup] Expected {} at {}, got {}", type.getSimpleName(), pos, be == null ? "null" : be.getClass().getSimpleName());
            return null;
         } else {
            return type.cast(be);
         }
      }
   }
}

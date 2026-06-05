package buildcraft.lib.fabric.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.jspecify.annotations.Nullable;

import buildcraft.api.core.BCLog;

public final class MenuBlockEntityLookup {
    private MenuBlockEntityLookup() {}

    public static <T extends BlockEntity> @Nullable T get(Inventory playerInv, BlockPos pos, Class<T> type) {
        if (pos == null) {
            BCLog.logger.warn("[menu.lookup] Missing BlockPos for {}", type.getSimpleName());
            return null;
        }
        if (playerInv.player.level() == null) {
            BCLog.logger.warn("[menu.lookup] Missing level for {} at {}", type.getSimpleName(), pos);
            return null;
        }

        if (!playerInv.player.level().hasChunkAt(pos)) {
            BCLog.logger.warn("[menu.lookup] Chunk not loaded for {} at {}", type.getSimpleName(), pos);
            return null;
        }
        BlockEntity be = playerInv.player.level().getBlockEntity(pos);
        if (!type.isInstance(be)) {
            BCLog.logger.warn("[menu.lookup] Expected {} at {}, got {}",
                    type.getSimpleName(),
                    pos,
                    be == null ? "null" : be.getClass().getSimpleName());
            return null;
        }
        return type.cast(be);
    }
}

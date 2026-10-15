package buildcraft.lib.fabric;

import buildcraft.lib.tile.TileMarker;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;

public final class BCBlockEntityLifecycleEvents {
   private static boolean initialized;

   private BCBlockEntityLifecycleEvents() {
   }

   public static void init() {
      if (initialized) {
         return;
      }

      initialized = true;
      ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.register((blockEntity, level) -> {
         if (blockEntity instanceof TileMarker<?> marker) {
            marker.buildcraft$onAttachedToLevel(level);
         }
      });
      ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register((blockEntity, level) -> {
         if (blockEntity instanceof TileMarker<?> marker) {
            BlockPos pos = blockEntity.getBlockPos();
            if (level.hasChunk(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ())) && level.getBlockState(pos).hasBlockEntity()) {
               marker.buildcraft$onChunkUnloading();
            }
         }
      });
   }
}

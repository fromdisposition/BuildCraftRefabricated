package buildcraft.lib.platform.client;

import buildcraft.silicon.tile.TileLaser;
import buildcraft.transport.tile.TilePipeHolder;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientBlockEntityEvents;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class BCClientBlockEntityLifecycleEvents {
   private static boolean initialized;

   private BCClientBlockEntityLifecycleEvents() {
   }

   public static void init() {
      if (initialized) {
         return;
      }

      initialized = true;
      ClientBlockEntityEvents.BLOCK_ENTITY_LOAD.register((blockEntity, level) -> onClientLoad(blockEntity));
      ClientBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register((blockEntity, level) -> onClientUnload(blockEntity));
   }

   private static void onClientLoad(BlockEntity blockEntity) {
      if (blockEntity instanceof TilePipeHolder pipeHolder) {
         pipeHolder.onLoad();
      } else if (blockEntity instanceof TileLaser laser) {
         laser.onLoad();
      }
   }

   private static void onClientUnload(BlockEntity blockEntity) {
      if (blockEntity instanceof TileLaser laser) {
         laser.onClientUnload();
      }
   }
}

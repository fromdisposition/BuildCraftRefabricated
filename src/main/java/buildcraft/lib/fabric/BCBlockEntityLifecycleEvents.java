package buildcraft.lib.fabric;

import buildcraft.builders.tile.TileBuilder;
import buildcraft.builders.tile.TileFiller;
import buildcraft.builders.tile.TileQuarry;
import buildcraft.factory.tile.TileFloodGate;
import buildcraft.factory.tile.TileMiner;
import buildcraft.factory.tile.TileTank;
import buildcraft.lib.tile.TileMarker;
import buildcraft.silicon.tile.TileLaser;
import buildcraft.transport.tile.TilePipeHolder;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;

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
         onServerLoad(blockEntity);
         if (blockEntity instanceof TileMarker<?> marker) {
            marker.buildcraft$onAttachedToLevel();
         }
      });
      ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register((blockEntity, level) -> {
         if (blockEntity instanceof TileMarker<?> marker) {
            BlockPos pos = blockEntity.getBlockPos();
            if (Mc26Compat.isChunkLoaded(level, pos) && level.getBlockState(pos).hasBlockEntity()) {
               marker.buildcraft$onChunkUnloading();
            }
         }
      });
   }

   private static void onServerLoad(BlockEntity blockEntity) {
      if (blockEntity instanceof TileTank tank) {
         tank.onLoad();
      } else if (blockEntity instanceof TileMiner miner) {
         miner.onLoad();
      } else if (blockEntity instanceof TileFloodGate floodGate) {
         floodGate.onLoad();
      } else if (blockEntity instanceof TileFiller filler) {
         filler.onLoad();
      } else if (blockEntity instanceof TileBuilder builder) {
         builder.onLoad();
      } else if (blockEntity instanceof TilePipeHolder pipeHolder) {
         pipeHolder.onLoad();
      } else if (blockEntity instanceof TileLaser laser) {
         laser.onLoad();
      } else if (blockEntity instanceof TileQuarry quarry) {
         quarry.onLoad();
      }
   }
}

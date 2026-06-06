package buildcraft.transport.net;

import buildcraft.lib.net.BcPacketDistributor;
import buildcraft.lib.net.PlayerBatchQueue;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class PipeItemMessageQueue {
   private static final PlayerBatchQueue<MessageMultiPipeItem> QUEUE = new PlayerBatchQueue<>(unused -> new MessageMultiPipeItem());

   private PipeItemMessageQueue() {
   }

   public static void serverTick() {
      Profiler.get().push("buildcraft:pipe_item_flush");

      try {
         QUEUE.flushEach(entry -> {
            if (!entry.getValue().items.isEmpty()) {
               BcPacketDistributor.sendToPlayer(entry.getKey(), entry.getValue());
            }
         });
      } finally {
         Profiler.get().pop();
      }
   }

   public static void appendTravellingItem(
      Level world, BlockPos pos, ItemStack stack, int stackCount, boolean toCenter, Direction side, @Nullable DyeColor colour, int timeToDest
   ) {
      if (world instanceof ServerLevel server) {
         QUEUE.forTracking(server, pos, (player, packet) -> packet.append(pos, stack, stackCount, toCenter, side, colour, timeToDest));
      }
   }
}

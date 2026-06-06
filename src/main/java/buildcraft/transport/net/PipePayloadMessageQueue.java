package buildcraft.transport.net;

import buildcraft.lib.net.BcPacketDistributor;
import buildcraft.lib.net.PlayerBatchQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.profiling.Profiler;

public final class PipePayloadMessageQueue {
   private static final PlayerBatchQueue<PipePayloadCoalescer> TRACKING_QUEUE = new PlayerBatchQueue<>(unused -> new PipePayloadCoalescer());
   private static final PlayerBatchQueue<PipePayloadCoalescer> GUI_QUEUE = new PlayerBatchQueue<>(unused -> new PipePayloadCoalescer());

   private PipePayloadMessageQueue() {
   }

   public static void enqueueTracking(ServerLevel level, BlockPos pos, MessagePipePayload payload) {
      TRACKING_QUEUE.forTracking(level, pos, (player, coalescer) -> coalescer.put(payload));
   }

   public static void enqueueGui(ServerPlayer player, MessagePipePayload payload) {
      GUI_QUEUE.getOrCreate(player).put(payload);
   }

   public static void serverTick() {
      Profiler.get().push("buildcraft:pipe_payload_flush");

      try {
         flushQueue(TRACKING_QUEUE);
         flushQueue(GUI_QUEUE);
      } finally {
         Profiler.get().pop();
      }
   }

   private static void flushQueue(PlayerBatchQueue<PipePayloadCoalescer> queue) {
      List<Entry<ServerPlayer, PipePayloadCoalescer>> snapshot = new ArrayList<>();

      for (Entry<ServerPlayer, PipePayloadCoalescer> entry : queue.entries()) {
         snapshot.add(entry);
      }

      for (Entry<ServerPlayer, PipePayloadCoalescer> entry : snapshot) {
         PipePayloadCoalescer coalescer = entry.getValue();

         while (!coalescer.isEmpty()) {
            List<MessagePipePayload> payloads = coalescer.drain();
            if (payloads.isEmpty()) {
               break;
            }

            if (payloads.size() == 1) {
               BcPacketDistributor.sendToPlayer(entry.getKey(), payloads.getFirst());
            } else {
               BcPacketDistributor.sendToPlayer(entry.getKey(), new MessageMultiPipePayload(payloads));
            }
         }

         if (coalescer.isEmpty()) {
            queue.remove(entry.getKey());
         }
      }
   }
}

package buildcraft.builders.snapshot;

import buildcraft.api.core.BCLog;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class BuildersNetworkAsync {
   private static final int MAX_QUEUE = 32;
   private static final Map<UUID, Boolean> SNAPSHOT_INFLIGHT = new ConcurrentHashMap<>();
   private static final ExecutorService SERVER_COMPRESS = createPool("BC Builders Server %d", 1, Math.max(2, Runtime.getRuntime().availableProcessors() / 4));
   private static final ExecutorService CLIENT_DECOMPRESS = createPool("BC Builders Client %d", 1, Math.max(2, Runtime.getRuntime().availableProcessors() / 4));
   private static final ExecutorService DISK_WRITE = createPool("BC Builders Disk %d", 1, 1);
   private static final ExecutorService SERVER_SERIALIZE = createPool(
      "BC Builders Serialize %d", 1, Math.max(2, Runtime.getRuntime().availableProcessors() / 4)
   );

   private BuildersNetworkAsync() {
   }

   private static ExecutorService createPool(String namePattern, int core, int max) {
      return new ThreadPoolExecutor(
         core,
         max,
         60L,
         TimeUnit.SECONDS,
         new LinkedBlockingQueue<>(32),
         Thread.ofPlatform().name(namePattern, 0L).daemon().factory(),
         (r, executor) -> BCLog.logger.warn("[builders.network] {} queue full, dropping task", namePattern)
      );
   }

   public static boolean tryAcquireSnapshot(UUID playerId) {
      return SNAPSHOT_INFLIGHT.putIfAbsent(playerId, Boolean.TRUE) == null;
   }

   public static void releaseSnapshot(UUID playerId) {
      SNAPSHOT_INFLIGHT.remove(playerId);
   }

   public static void runServerCompress(Runnable task) {
      SERVER_COMPRESS.execute(task);
   }

   public static void runClientDecompress(Runnable task) {
      CLIENT_DECOMPRESS.execute(task);
   }

   public static void runDiskWrite(Runnable task) {
      DISK_WRITE.execute(task);
   }

   public static void runServerSerialize(Runnable task) {
      SERVER_SERIALIZE.execute(task);
   }
}

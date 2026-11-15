/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import buildcraft.api.core.BCLog;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class BuildersNetworkAsync {
   private static final int MAX_QUEUE = 32;
   private static final Map<UUID, Boolean> SNAPSHOT_INFLIGHT = new ConcurrentHashMap<>();
   private static final ExecutorService SERVER_COMPRESS = createPool("BC Builders Server %d", 1, Math.max(2, Runtime.getRuntime().availableProcessors() / 4));
   private static final ExecutorService CLIENT_DECOMPRESS = createPool("BC Builders Client %d", 1, Math.max(2, Runtime.getRuntime().availableProcessors() / 4));
   private static final ExecutorService DISK_WRITE = Executors.newSingleThreadExecutor(Thread.ofPlatform().name("BC Builders Disk").daemon().factory());
   private static final ExecutorService SERVER_SERIALIZE = createPool(
      "BC Builders Serialize %d", 1, Math.max(2, Runtime.getRuntime().availableProcessors() / 4)
   );

   private BuildersNetworkAsync() {
   }

   private static ExecutorService createPool(String namePattern, int core, int max) {
      return new ThreadPoolExecutor(
         core, max, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(MAX_QUEUE), Thread.ofPlatform().name(namePattern, 0L).daemon().factory()
      );
   }

   private static boolean submit(ExecutorService pool, String name, Runnable task) {
      try {
         pool.execute(task);
         return true;
      } catch (RejectedExecutionException e) {
         BCLog.logger.warn("[builders.network] {} queue full, dropping task", name);
         return false;
      }
   }

   public static boolean tryAcquireSnapshot(UUID playerId) {
      return SNAPSHOT_INFLIGHT.putIfAbsent(playerId, Boolean.TRUE) == null;
   }

   public static void releaseSnapshot(UUID playerId) {
      SNAPSHOT_INFLIGHT.remove(playerId);
   }

   public static boolean runServerCompress(Runnable task) {
      return submit(SERVER_COMPRESS, "BC Builders Server", task);
   }

   public static void runClientDecompress(Runnable task) {
      submit(CLIENT_DECOMPRESS, "BC Builders Client", task);
   }

   public static void runDiskWrite(Runnable task) {
      DISK_WRITE.execute(task);
   }
}

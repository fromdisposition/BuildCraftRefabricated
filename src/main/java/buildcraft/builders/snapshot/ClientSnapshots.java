package buildcraft.builders.snapshot;

import buildcraft.lib.misc.HashUtil;
import buildcraft.lib.net.BcPacketDistributor;
import buildcraft.lib.sync.ClientKeyedCache;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public enum ClientSnapshots {
   INSTANCE;

   private static final Logger LOGGER = LogManager.getLogger("BCClientSnapshots");
   private final ClientKeyedCache<Snapshot.Key, Snapshot> cache = new ClientKeyedCache<>(ClientSnapshots::requestSnapshot);

   private static void requestSnapshot(Snapshot.Key key) {
      String hashHex = key.hash == null ? "null" : HashUtil.convertHashToString(key.hash);
      LOGGER.info("Sending SnapshotRequest to server: hash={} hasHeader={}", hashHex, key.header != null);
      BcPacketDistributor.sendToServer(BuildersClientRequestPayload.snapshot(key));
   }

   public Snapshot getSnapshot(Snapshot.Key key) {
      return this.cache.get(key);
   }

   public void onSnapshotReceived(Snapshot snapshot) {
      String hashHex = snapshot.key.hash == null ? "null" : HashUtil.convertHashToString(snapshot.key.hash);
      LOGGER.info(
         "Received snapshot from server: class={} hash={} size={} pendingRemoved={}",
         snapshot.getClass().getSimpleName(),
         hashHex,
         snapshot.size,
         this.cache.isPending(snapshot.key)
      );
      this.cache.put(snapshot.key, snapshot);
   }
}

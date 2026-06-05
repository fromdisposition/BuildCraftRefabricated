package buildcraft.builders.snapshot;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public enum ClientSnapshots {
    INSTANCE;

    private static final Logger LOGGER = LogManager.getLogger("BCClientSnapshots");

    private final List<Snapshot> snapshots = new ArrayList<>();
    private final List<Snapshot.Key> pending = new ArrayList<>();

    public Snapshot getSnapshot(Snapshot.Key key) {
        Snapshot found = snapshots.stream().filter(snapshot -> snapshot.key.equals(key)).findFirst().orElse(null);
        if (found == null && !pending.contains(key)) {
            pending.add(key);
            String hashHex = key.hash == null ? "null"
                    : buildcraft.lib.misc.HashUtil.convertHashToString(key.hash);
            LOGGER.info("Sending SnapshotRequest to server: hash={} hasHeader={}",
                    hashHex, key.header != null);
            buildcraft.fabric.client.ClientPacketDistributor.sendToServer(new SnapshotRequestPayload(key));
        }
        return found;
    }

    public void onSnapshotReceived(Snapshot snapshot) {
        String hashHex = snapshot.key.hash == null ? "null"
                : buildcraft.lib.misc.HashUtil.convertHashToString(snapshot.key.hash);
        LOGGER.info("Received snapshot from server: class={} hash={} size={} pendingRemoved={}",
                snapshot.getClass().getSimpleName(), hashHex, snapshot.size,
                pending.remove(snapshot.key));

        snapshots.removeIf(s -> s.key.equals(snapshot.key));
        snapshots.add(snapshot);
    }
}

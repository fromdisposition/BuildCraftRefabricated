package buildcraft.builders.snapshot;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SnapshotRequestPayload(Snapshot.Key key) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SnapshotRequestPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.parse("buildcraftrefabricated:snapshot_request"));

    public static final StreamCodec<FriendlyByteBuf, SnapshotRequestPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeByteArray(payload.key().hash);
                buf.writeBoolean(payload.key().header != null);
                if (payload.key().header != null) {
                    buf.writeUUID(payload.key().header.owner);
                    buf.writeLong(payload.key().header.created.getTime());
                    buf.writeUtf(payload.key().header.name);
                }
            },
            buf -> {
                byte[] hash = buf.readByteArray();
                boolean hasHeader = buf.readBoolean();
                Snapshot.Key key = new Snapshot.Key(new Snapshot.Key(), hash);
                if (hasHeader) {
                    Snapshot.Header header = new Snapshot.Header(key, buf.readUUID(), new java.util.Date(buf.readLong()), buf.readUtf());
                    key = new Snapshot.Key(key, header);
                }
                return new SnapshotRequestPayload(key);
            }
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private static final org.apache.logging.log4j.Logger LOGGER =
            org.apache.logging.log4j.LogManager.getLogger("BCSnapshotRequest");

    public static void handle(SnapshotRequestPayload payload, buildcraft.fabric.network.BCPayloadContext context) {
        String hashHex = payload.key().hash == null ? "null"
                : buildcraft.lib.misc.HashUtil.convertHashToString(payload.key().hash);
        net.minecraft.world.level.Level level = context.player().level();
        Snapshot snapshot = GlobalSavedDataSnapshots.get(level).getSnapshot(payload.key());
        if (snapshot != null) {
            LOGGER.info("Replying with snapshot: class={} hash={} size={} (player level={})",
                    snapshot.getClass().getSimpleName(), hashHex, snapshot.size,
                    level.dimension());
            context.reply(new SnapshotResponsePayload(snapshot));
        } else {
            LOGGER.warn("Snapshot NOT FOUND on server: hash={} (player level={}). "
                    + "Saved data is per-dimension — blueprint may have been created in a different dimension.",
                    hashHex, level.dimension());
        }
    }
}

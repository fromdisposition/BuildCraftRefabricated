package buildcraft.builders.snapshot;

import java.io.IOException;

import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import buildcraft.lib.net.BoundedNbt;

public record SnapshotResponsePayload(Snapshot snapshot) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SnapshotResponsePayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.parse("buildcraftrefabricated:snapshot_response"));

    public static final StreamCodec<FriendlyByteBuf, SnapshotResponsePayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                try {
                    CompoundTag nbt = Snapshot.writeToNBT(payload.snapshot());
                    net.minecraft.nbt.NbtIo.writeCompressed(nbt, new ByteBufOutputStream(buf));
                } catch (IOException e) {
                    throw new RuntimeException("Failed to compress snapshot", e);
                }
            },
            buf -> {
                try {
                    CompoundTag nbt = net.minecraft.nbt.NbtIo.readCompressed(new ByteBufInputStream(buf),
                            BoundedNbt.networkQuota());
                    return new SnapshotResponsePayload(Snapshot.readFromNBT(nbt));
                } catch (Exception e) {
                    throw new RuntimeException("Failed to decompress snapshot", e);
                }
            }
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SnapshotResponsePayload payload, buildcraft.fabric.network.BCPayloadContext context) {
        ClientSnapshots.INSTANCE.onSnapshotReceived(payload.snapshot());
    }
}

package buildcraft.builders.snapshot;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import buildcraft.lib.net.BCPacketLimits;

public record ArchitectScanPayload(List<BlockPos> positions) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ArchitectScanPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.parse("buildcraftrefabricated:architect_scan"));

    public static final StreamCodec<FriendlyByteBuf, ArchitectScanPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeVarInt(payload.positions().size());
                for (BlockPos pos : payload.positions()) {
                    buf.writeBlockPos(pos);
                }
            },
            buf -> {
                int count = BCPacketLimits.validateCount(buf.readVarInt(),
                        BCPacketLimits.MAX_ARCHITECT_SCAN_POSITIONS, "architect scan");
                List<BlockPos> list = new ArrayList<>(count);
                for (int i = 0; i < count; i++) {
                    list.add(buf.readBlockPos());
                }
                return new ArchitectScanPayload(list);
            }
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ArchitectScanPayload payload,
                              buildcraft.fabric.network.BCPayloadContext context) {
        ClientArchitectScans.INSTANCE.onReceived(payload.positions());
    }
}

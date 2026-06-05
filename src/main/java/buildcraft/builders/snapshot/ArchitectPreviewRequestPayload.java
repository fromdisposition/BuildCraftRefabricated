package buildcraft.builders.snapshot;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import buildcraft.builders.tile.TileArchitectTable;

public record ArchitectPreviewRequestPayload(BlockPos architectPos) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ArchitectPreviewRequestPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.parse("buildcraftrefabricated:architect_preview_request"));

    public static final StreamCodec<FriendlyByteBuf, ArchitectPreviewRequestPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> buf.writeBlockPos(payload.architectPos()),
            buf -> new ArchitectPreviewRequestPayload(buf.readBlockPos())
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ArchitectPreviewRequestPayload payload,
                              buildcraft.fabric.network.BCPayloadContext context) {
        net.minecraft.world.entity.player.Player player = context.player();
        Level level = player.level();

        BlockPos pos = payload.architectPos();
        if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > 64.0) {
            context.reply(new ArchitectPreviewResponsePayload(pos, null));
            return;
        }

        BlockEntity be = level.getBlockEntity(pos);
        Blueprint preview = null;
        if (be instanceof TileArchitectTable architect) {
            preview = architect.getOrRefreshLivePreview();
        }

        context.reply(new ArchitectPreviewResponsePayload(pos, preview));
    }
}

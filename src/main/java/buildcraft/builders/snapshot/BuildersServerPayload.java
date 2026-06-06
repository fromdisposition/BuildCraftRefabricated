package buildcraft.builders.snapshot;

import buildcraft.fabric.network.BCPayloadContext;
import buildcraft.lib.net.BCPacketLimits;
import buildcraft.lib.net.BoundedNbt;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;

public record BuildersServerPayload(
   BuildersServerPayload.Kind kind, @Nullable byte[] snapshotData, BlockPos architectPos, @Nullable byte[] previewData, List<BlockPos> scanPositions
) implements CustomPacketPayload {
   public static final Type<BuildersServerPayload> TYPE = new Type(Identifier.parse("buildcraftrefabricated:builders_server"));
   public static final StreamCodec<FriendlyByteBuf, BuildersServerPayload> STREAM_CODEC = StreamCodec.of(
      BuildersServerPayload::encode, BuildersServerPayload::decode
   );

   public static BuildersServerPayload snapshot(byte[] compressed) {
      return new BuildersServerPayload(BuildersServerPayload.Kind.SNAPSHOT, compressed, BlockPos.ZERO, null, List.of());
   }

   public static BuildersServerPayload architectPreview(BlockPos pos, @Nullable byte[] compressed) {
      return new BuildersServerPayload(BuildersServerPayload.Kind.ARCHITECT_PREVIEW, null, pos, compressed, List.of());
   }

   public static BuildersServerPayload architectScan(List<BlockPos> positions) {
      return new BuildersServerPayload(BuildersServerPayload.Kind.ARCHITECT_SCAN, null, BlockPos.ZERO, null, List.copyOf(positions));
   }

   @Nullable
   public static byte[] compressSnapshot(Snapshot snapshot) {
      try {
         CompoundTag nbt = Snapshot.writeToNBT(snapshot);
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         NbtIo.writeCompressed(nbt, out);
         return out.toByteArray();
      } catch (IOException e) {
         throw new RuntimeException("Failed to compress snapshot", e);
      }
   }

   @Nullable
   public static byte[] compressBlueprint(@Nullable Blueprint blueprint) {
      if (blueprint == null) {
         return null;
      }

      try {
         CompoundTag nbt = Snapshot.writeToNBT(blueprint);
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         NbtIo.writeCompressed(nbt, out);
         return out.toByteArray();
      } catch (IOException e) {
         throw new RuntimeException("Failed to compress architect preview", e);
      }
   }

   private static void encode(FriendlyByteBuf buf, BuildersServerPayload payload) {
      buf.writeEnum(payload.kind());
      switch (payload.kind()) {
         case SNAPSHOT:
            buf.writeByteArray(payload.snapshotData());
            break;
         case ARCHITECT_PREVIEW:
            buf.writeBlockPos(payload.architectPos());
            buf.writeBoolean(payload.previewData() != null);
            if (payload.previewData() != null) {
               buf.writeByteArray(payload.previewData());
            }
            break;
         case ARCHITECT_SCAN:
            buf.writeVarInt(payload.scanPositions().size());

            for (BlockPos pos : payload.scanPositions()) {
               buf.writeBlockPos(pos);
            }
      }
   }

   private static BuildersServerPayload decode(FriendlyByteBuf buf) {
      BuildersServerPayload.Kind kind = (BuildersServerPayload.Kind)buf.readEnum(BuildersServerPayload.Kind.class);

      return switch (kind) {
         case SNAPSHOT -> new BuildersServerPayload(kind, buf.readByteArray(), BlockPos.ZERO, null, List.of());
         case ARCHITECT_PREVIEW -> {
            BlockPos pos = buf.readBlockPos();
            byte[] data = buf.readBoolean() ? buf.readByteArray() : null;
            yield new BuildersServerPayload(kind, null, pos, data, List.of());
         }
         case ARCHITECT_SCAN -> {
            int count = BCPacketLimits.validateCount(buf.readVarInt(), 500000, "architect scan");
            List<BlockPos> list = new ArrayList<>(count);

            for (int i = 0; i < count; i++) {
               list.add(buf.readBlockPos());
            }

            yield new BuildersServerPayload(kind, null, BlockPos.ZERO, null, list);
         }
      };
   }

   public Type<BuildersServerPayload> type() {
      return TYPE;
   }

   public static void handle(BuildersServerPayload payload, BCPayloadContext context) {
      switch (payload.kind()) {
         case SNAPSHOT:
            handleSnapshot(payload.snapshotData(), context);
            break;
         case ARCHITECT_PREVIEW:
            handleArchitectPreview(payload.architectPos(), payload.previewData(), context);
            break;
         case ARCHITECT_SCAN:
            context.enqueueWork(() -> ClientArchitectScans.INSTANCE.onReceived(payload.scanPositions()));
      }
   }

   private static void handleSnapshot(@Nullable byte[] data, BCPayloadContext context) {
      if (data != null && data.length != 0) {
         BuildersNetworkAsync.runClientDecompress(() -> {
            try {
               Snapshot snapshot = decompressSnapshot(data);
               context.enqueueWork(() -> ClientSnapshots.INSTANCE.onSnapshotReceived(snapshot));
            } catch (IOException e) {
               throw new RuntimeException(e);
            }
         });
      }
   }

   private static void handleArchitectPreview(BlockPos pos, @Nullable byte[] data, BCPayloadContext context) {
      if (data == null) {
         context.enqueueWork(() -> ClientArchitectPreviews.INSTANCE.onReceived(pos, null));
      } else {
         BuildersNetworkAsync.runClientDecompress(() -> {
            try {
               Blueprint blueprint = decompressBlueprint(data);
               context.enqueueWork(() -> ClientArchitectPreviews.INSTANCE.onReceived(pos, blueprint));
            } catch (IOException e) {
               throw new RuntimeException(e);
            }
         });
      }
   }

   private static Snapshot decompressSnapshot(byte[] data) throws IOException {
      CompoundTag nbt = NbtIo.readCompressed(new ByteArrayInputStream(data), BoundedNbt.networkQuota());
      return Snapshot.readFromNBT(nbt);
   }

   @Nullable
   private static Blueprint decompressBlueprint(byte[] data) throws IOException {
      CompoundTag nbt = NbtIo.readCompressed(new ByteArrayInputStream(data), BoundedNbt.networkQuota());
      return Snapshot.readFromNBT(nbt) instanceof Blueprint blueprint ? blueprint : null;
   }

   public enum Kind {
      SNAPSHOT,
      ARCHITECT_PREVIEW,
      ARCHITECT_SCAN;
   }
}

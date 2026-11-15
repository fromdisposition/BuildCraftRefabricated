/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import buildcraft.lib.net.BcPayloadBuffers;
import buildcraft.fabric.network.BCPayloadContext;
import buildcraft.lib.net.BCPacketLimits;
import buildcraft.lib.net.BoundedNbt;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jspecify.annotations.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;

public record BuildersServerPayload(
   BuildersServerPayload.Kind kind, @Nullable byte[] snapshotData, BlockPos architectPos, @Nullable byte[] previewData, List<BlockPos> scanPositions, boolean last
) implements CustomPacketPayload {
   public static final Type<BuildersServerPayload> TYPE = new Type<>(Identifier.parse("buildcraftrefabricated:builders_server"));
   public static final StreamCodec<FriendlyByteBuf, BuildersServerPayload> STREAM_CODEC = StreamCodec.of(
      BuildersServerPayload::encode, BuildersServerPayload::decode
   );
   private static final List<byte[]> SNAPSHOT_PARTS = new ArrayList<>();
   private static final List<byte[]> PREVIEW_PARTS = new ArrayList<>();

   public static List<BuildersServerPayload> snapshot(byte[] compressed) {
      List<byte[]> parts = split(compressed);
      List<BuildersServerPayload> payloads = new ArrayList<>(parts.size());

      for (int i = 0; i < parts.size(); i++) {
         payloads.add(new BuildersServerPayload(BuildersServerPayload.Kind.SNAPSHOT, parts.get(i), BlockPos.ZERO, null, List.of(), i == parts.size() - 1));
      }

      return payloads;
   }

   public static List<BuildersServerPayload> architectPreview(BlockPos pos, @Nullable byte[] compressed) {
      if (compressed == null) {
         return List.of(new BuildersServerPayload(BuildersServerPayload.Kind.ARCHITECT_PREVIEW, null, pos, null, List.of(), true));
      }

      List<byte[]> parts = split(compressed);
      List<BuildersServerPayload> payloads = new ArrayList<>(parts.size());

      for (int i = 0; i < parts.size(); i++) {
         payloads.add(new BuildersServerPayload(BuildersServerPayload.Kind.ARCHITECT_PREVIEW, null, pos, parts.get(i), List.of(), i == parts.size() - 1));
      }

      return payloads;
   }

   public static BuildersServerPayload architectScan(List<BlockPos> positions) {
      return new BuildersServerPayload(BuildersServerPayload.Kind.ARCHITECT_SCAN, null, BlockPos.ZERO, null, List.copyOf(positions), true);
   }

   private static List<byte[]> split(byte[] data) {
      List<byte[]> parts = new ArrayList<>();

      for (int offset = 0; offset < data.length; offset += BCPacketLimits.MAX_CHUNK_BYTES) {
         parts.add(Arrays.copyOfRange(data, offset, Math.min(data.length, offset + BCPacketLimits.MAX_CHUNK_BYTES)));
      }

      if (parts.isEmpty()) {
         parts.add(new byte[0]);
      }

      return parts;
   }

   @Nullable
   private static byte[] accumulate(List<byte[]> parts, byte[] part, boolean last) {
      parts.add(part);
      int total = parts.stream().mapToInt(p -> p.length).sum();
      if (total > BCPacketLimits.MAX_ASSEMBLED_BYTES) {
         parts.clear();
         return null;
      }

      if (!last) {
         return null;
      }

      byte[] assembled = new byte[total];
      int pos = 0;

      for (byte[] p : parts) {
         System.arraycopy(p, 0, assembled, pos, p.length);
         pos += p.length;
      }

      parts.clear();
      return assembled;
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
      BcPayloadBuffers.writeEnum(buf, payload.kind());
      switch (payload.kind()) {
         case SNAPSHOT:
            buf.writeBoolean(payload.last());
            buf.writeByteArray(payload.snapshotData());
            break;
         case ARCHITECT_PREVIEW:
            buf.writeBlockPos(payload.architectPos());
            buf.writeBoolean(payload.previewData() != null);
            if (payload.previewData() != null) {
               buf.writeBoolean(payload.last());
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
      BuildersServerPayload.Kind kind = BcPayloadBuffers.readEnum(buf, BuildersServerPayload.Kind.class);

      return switch (kind) {
         case SNAPSHOT -> {
            boolean last = buf.readBoolean();
            yield new BuildersServerPayload(kind, buf.readByteArray(BCPacketLimits.MAX_CHUNK_BYTES), BlockPos.ZERO, null, List.of(), last);
         }
         case ARCHITECT_PREVIEW -> {
            BlockPos pos = buf.readBlockPos();
            if (!buf.readBoolean()) {
               yield new BuildersServerPayload(kind, null, pos, null, List.of(), true);
            }

            boolean last = buf.readBoolean();
            yield new BuildersServerPayload(kind, null, pos, buf.readByteArray(BCPacketLimits.MAX_CHUNK_BYTES), List.of(), last);
         }
         case ARCHITECT_SCAN -> {
            int count = BCPacketLimits.validateCount(buf.readVarInt(), 500000, "architect scan");
            List<BlockPos> list = new ArrayList<>(count);

            for (int i = 0; i < count; i++) {
               list.add(buf.readBlockPos());
            }

            yield new BuildersServerPayload(kind, null, BlockPos.ZERO, null, list, true);
         }
      };
   }

   @Override
   public Type<BuildersServerPayload> type() {
      return TYPE;
   }

   public static void handle(BuildersServerPayload payload, BCPayloadContext context) {
      switch (payload.kind()) {
         case SNAPSHOT:
            handleSnapshot(accumulate(SNAPSHOT_PARTS, payload.snapshotData(), payload.last()), context);
            break;
         case ARCHITECT_PREVIEW:
            if (payload.previewData() == null) {
               handleArchitectPreview(payload.architectPos(), null, context);
            } else {
               byte[] assembled = accumulate(PREVIEW_PARTS, payload.previewData(), payload.last());
               if (assembled != null) {
                  handleArchitectPreview(payload.architectPos(), assembled, context);
               }
            }
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

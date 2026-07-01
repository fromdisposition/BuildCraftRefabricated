/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import buildcraft.builders.tile.TileArchitectTable;
import buildcraft.fabric.network.BCPayloadContext;
import buildcraft.lib.misc.HashUtil;
import java.util.Date;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public record BuildersClientRequestPayload(BuildersClientRequestPayload.Kind kind, Snapshot.Key snapshotKey, BlockPos architectPos)
   implements CustomPacketPayload {
   public static final Type<BuildersClientRequestPayload> TYPE = new Type<>(Identifier.parse("buildcraftrefabricated:builders_client_request"));
   public static final StreamCodec<FriendlyByteBuf, BuildersClientRequestPayload> STREAM_CODEC = StreamCodec.of(
      BuildersClientRequestPayload::encode, BuildersClientRequestPayload::decode
   );
   private static final Logger LOGGER = LogManager.getLogger("BCBuildersClientRequest");

   public static BuildersClientRequestPayload snapshot(Snapshot.Key key) {
      return new BuildersClientRequestPayload(BuildersClientRequestPayload.Kind.SNAPSHOT, key, BlockPos.ZERO);
   }

   public static BuildersClientRequestPayload architectPreview(BlockPos pos) {
      return new BuildersClientRequestPayload(BuildersClientRequestPayload.Kind.ARCHITECT_PREVIEW, null, pos);
   }

   private static void encode(FriendlyByteBuf buf, BuildersClientRequestPayload payload) {
      buf.writeEnum(payload.kind());
      switch (payload.kind()) {
         case SNAPSHOT:
            writeSnapshotKey(buf, payload.snapshotKey());
            break;
         case ARCHITECT_PREVIEW:
            buf.writeBlockPos(payload.architectPos());
      }
   }

   private static BuildersClientRequestPayload decode(FriendlyByteBuf buf) {
      BuildersClientRequestPayload.Kind kind = (BuildersClientRequestPayload.Kind)buf.readEnum(BuildersClientRequestPayload.Kind.class);

      return switch (kind) {
         case SNAPSHOT -> new BuildersClientRequestPayload(kind, readSnapshotKey(buf), BlockPos.ZERO);
         case ARCHITECT_PREVIEW -> new BuildersClientRequestPayload(kind, null, buf.readBlockPos());
      };
   }

   private static void writeSnapshotKey(FriendlyByteBuf buf, Snapshot.Key key) {
      buf.writeByteArray(key.hash);
      buf.writeBoolean(key.header != null);
      if (key.header != null) {
         buf.writeUUID(key.header.owner);
         buf.writeLong(key.header.created.getTime());
         buf.writeUtf(key.header.name);
      }
   }

   private static Snapshot.Key readSnapshotKey(FriendlyByteBuf buf) {
      byte[] hash = buf.readByteArray();
      boolean hasHeader = buf.readBoolean();
      Snapshot.Key key = new Snapshot.Key(new Snapshot.Key(), hash);
      if (hasHeader) {
         Snapshot.Header header = new Snapshot.Header(key, buf.readUUID(), new Date(buf.readLong()), buf.readUtf());
         key = new Snapshot.Key(key, header);
      }

      return key;
   }

   public Type<BuildersClientRequestPayload> type() {
      return TYPE;
   }

   public static void handle(BuildersClientRequestPayload payload, BCPayloadContext context) {
      switch (payload.kind()) {
         case SNAPSHOT:
            handleSnapshot(payload.snapshotKey(), context);
            break;
         case ARCHITECT_PREVIEW:
            handleArchitectPreview(payload.architectPos(), context);
      }
   }

   private static void handleSnapshot(Snapshot.Key key, BCPayloadContext context) {
      Player player = context.player();
      if (player instanceof ServerPlayer serverPlayer) {
         if (!BuildersNetworkAsync.tryAcquireSnapshot(serverPlayer.getUUID())) {
            LOGGER.warn("Snapshot request throttled for player {} (already in flight)", serverPlayer.getName().getString());
         } else {
            String hashHex = key.hash == null ? "null" : HashUtil.convertHashToString(key.hash);
            Level level = player.level();
            Snapshot snapshot = GlobalSavedDataSnapshots.get(level).getSnapshot(key);
            if (snapshot == null) {
               BuildersNetworkAsync.releaseSnapshot(serverPlayer.getUUID());
               LOGGER.warn("Snapshot NOT FOUND on server: hash={} (player level={})", hashHex, level.dimension());
            } else {
               LOGGER.info("Preparing snapshot reply: class={} hash={} size={}", snapshot.getClass().getSimpleName(), hashHex, snapshot.size);
               BuildersNetworkAsync.runServerCompress(() -> {
                  try {
                     byte[] compressed = BuildersServerPayload.compressSnapshot(snapshot);
                     context.enqueueWork(() -> {
                        try {
                           if (compressed != null) {
                              context.reply(BuildersServerPayload.snapshot(compressed));
                           }
                        } finally {
                           BuildersNetworkAsync.releaseSnapshot(serverPlayer.getUUID());
                        }
                     });
                  } catch (RuntimeException e) {
                     BuildersNetworkAsync.releaseSnapshot(serverPlayer.getUUID());
                     throw e;
                  }
               });
            }
         }
      }
   }

   private static void handleArchitectPreview(BlockPos pos, BCPayloadContext context) {
      Player player = context.player();
      Level level = player.level();
      BlockPos architectPos = pos.immutable();
      if (player.distanceToSqr(architectPos.getX() + 0.5, architectPos.getY() + 0.5, architectPos.getZ() + 0.5) > 64.0) {
         context.reply(BuildersServerPayload.architectPreview(architectPos, null));
      } else {
         BlockEntity be = level.getBlockEntity(architectPos);
         Blueprint preview = null;
         if (be instanceof TileArchitectTable architect) {
            preview = architect.getOrRefreshLivePreview();
         }

         Blueprint finalPreview = preview;
         BuildersNetworkAsync.runServerCompress(() -> {
            byte[] compressed = BuildersServerPayload.compressBlueprint(finalPreview);
            context.enqueueWork(() -> context.reply(BuildersServerPayload.architectPreview(architectPos, compressed)));
         });
      }
   }

   public enum Kind {
      SNAPSHOT,
      ARCHITECT_PREVIEW;
   }
}

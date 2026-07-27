package buildcraft.fabric.network;

import buildcraft.builders.snapshot.BuildersClientRequestPayload;
import buildcraft.builders.snapshot.BuildersServerPayload;
import buildcraft.core.marker.volume.MessageVolumeBoxes;
import buildcraft.fabric.FabricPayloadContexts;
import buildcraft.lib.net.MessageContainerPayload;
import buildcraft.lib.net.MessageDebugRequest;
import buildcraft.lib.net.MessageDebugResponse;
import buildcraft.lib.net.MessageMarker;
import buildcraft.transport.net.MessageMultiPipeItem;
import buildcraft.transport.net.MessageMultiPipePayload;
import buildcraft.transport.net.MessagePipePayload;
import buildcraft.transport.net.MessageRemovePipePart;
import buildcraft.transport.wire.PayloadWireSync;
import java.util.function.BiConsumer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;

public final class BCNetworkingRegistry {
   private BCNetworkingRegistry() {
   }

   public static void registerCommon() {
      registerCodecClientbound(MessageMarker.TYPE, MessageMarker.STREAM_CODEC);
      registerCodecClientbound(MessageVolumeBoxes.TYPE, MessageVolumeBoxes.STREAM_CODEC);
      registerCodecClientbound(MessageDebugResponse.TYPE, MessageDebugResponse.STREAM_CODEC);
      registerCodecClientbound(MessageContainerPayload.TYPE, MessageContainerPayload.STREAM_CODEC);
      registerCodecClientbound(MessageMultiPipeItem.TYPE, MessageMultiPipeItem.STREAM_CODEC);
      registerCodecClientbound(MessagePipePayload.TYPE, MessagePipePayload.STREAM_CODEC);
      registerCodecClientbound(MessageMultiPipePayload.TYPE, MessageMultiPipePayload.STREAM_CODEC);
      registerCodecClientboundFriendly(PayloadWireSync.TYPE, PayloadWireSync.STREAM_CODEC);      registerCodecClientboundFriendly(BuildersServerPayload.TYPE, BuildersServerPayload.STREAM_CODEC);
      registerCodecServerbound(MessageDebugRequest.TYPE, MessageDebugRequest.STREAM_CODEC);
      registerCodecServerbound(MessageContainerPayload.TYPE, MessageContainerPayload.STREAM_CODEC);
      registerCodecServerbound(MessageRemovePipePart.TYPE, MessageRemovePipePart.STREAM_CODEC);
      registerCodecServerboundFriendly(BuildersClientRequestPayload.TYPE, BuildersClientRequestPayload.STREAM_CODEC);
   }

   public static void registerServer() {
      registerServerbound(MessageDebugRequest.TYPE, MessageDebugRequest.STREAM_CODEC, MessageDebugRequest::handle);
      registerServerbound(MessageContainerPayload.TYPE, MessageContainerPayload.STREAM_CODEC, MessageContainerPayload::handle);
      registerServerbound(MessageRemovePipePart.TYPE, MessageRemovePipePart.STREAM_CODEC, MessageRemovePipePart::handle);
      registerServerboundFriendly(BuildersClientRequestPayload.TYPE, BuildersClientRequestPayload.STREAM_CODEC, BuildersClientRequestPayload::handle);
   }

   // NOTE: clientbound RECEIVERS (which need net.fabricmc...client.ClientPlayNetworking + Minecraft) live in
   // BCNetworkingRegistryClient (@Environment CLIENT). This class is loaded on the dedicated server, so it must
   // not reference any client-only type. The clientbound CODECS above are registered in registerCommon() so
   // both sides agree on the wire format.

   private static <T extends CustomPacketPayload> void registerCodecClientbound(Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
      PayloadTypeRegistry.clientboundPlay().register(type, codec);
   }

   private static <T extends CustomPacketPayload> void registerCodecServerbound(Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
      PayloadTypeRegistry.serverboundPlay().register(type, codec);
   }

   private static <T extends CustomPacketPayload> void registerCodecClientboundFriendly(Type<T> type, StreamCodec<? super FriendlyByteBuf, T> codec) {
      PayloadTypeRegistry.clientboundPlay().register(type, codec);
   }

   private static <T extends CustomPacketPayload> void registerCodecServerboundFriendly(Type<T> type, StreamCodec<? super FriendlyByteBuf, T> codec) {
      PayloadTypeRegistry.serverboundPlay().register(type, codec);
   }

   // package-private: shared by BCNetworkingRegistryClient (same package) for clientbound dispatch.
   static <T extends CustomPacketPayload> void dispatch(T payload, BCPayloadContext ctx, BiConsumer<T, BCPayloadContext> handler) {
      ctx.enqueueWork(() -> handler.accept(payload, ctx));
   }

   private static <T extends CustomPacketPayload> void registerServerbound(
      Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec, BiConsumer<T, BCPayloadContext> handler
   ) {
      ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) -> {
         BCPayloadContext ctx = FabricPayloadContexts.of(context.player());
         dispatch((T)payload, ctx, handler);
      });
   }

   private static <T extends CustomPacketPayload> void registerServerboundFriendly(
      Type<T> type, StreamCodec<? super FriendlyByteBuf, T> codec, BiConsumer<T, BCPayloadContext> handler
   ) {
      ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) -> {
         BCPayloadContext ctx = FabricPayloadContexts.of(context.player());
         dispatch((T)payload, ctx, handler);
      });
   }
}

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
import buildcraft.transport.net.MessagePipeLandingEffect;
import buildcraft.transport.net.MessagePipePayload;
import buildcraft.transport.wire.PayloadWireSync;
import java.util.function.BiConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
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
      registerCodecClientboundFriendly(PayloadWireSync.TYPE, PayloadWireSync.STREAM_CODEC);
      registerCodecClientbound(MessagePipeLandingEffect.TYPE, MessagePipeLandingEffect.STREAM_CODEC);
      registerCodecClientboundFriendly(BuildersServerPayload.TYPE, BuildersServerPayload.STREAM_CODEC);
      registerCodecServerbound(MessageDebugRequest.TYPE, MessageDebugRequest.STREAM_CODEC);
      registerCodecServerbound(MessageContainerPayload.TYPE, MessageContainerPayload.STREAM_CODEC);
      registerCodecServerboundFriendly(BuildersClientRequestPayload.TYPE, BuildersClientRequestPayload.STREAM_CODEC);
   }

   public static void registerServer() {
      registerServerbound(MessageDebugRequest.TYPE, MessageDebugRequest.STREAM_CODEC, MessageDebugRequest::handle);
      registerServerbound(MessageContainerPayload.TYPE, MessageContainerPayload.STREAM_CODEC, MessageContainerPayload::handle);
      registerServerboundFriendly(BuildersClientRequestPayload.TYPE, BuildersClientRequestPayload.STREAM_CODEC, BuildersClientRequestPayload::handle);
   }

   public static void registerClient() {
      registerClientbound(MessageMarker.TYPE, MessageMarker.STREAM_CODEC, MessageMarker::handle);
      registerClientbound(MessageVolumeBoxes.TYPE, MessageVolumeBoxes.STREAM_CODEC, MessageVolumeBoxes::handle);
      registerClientbound(MessageDebugResponse.TYPE, MessageDebugResponse.STREAM_CODEC, MessageDebugResponse::handle);
      registerClientbound(MessageContainerPayload.TYPE, MessageContainerPayload.STREAM_CODEC, MessageContainerPayload::handle);
      registerClientbound(MessageMultiPipeItem.TYPE, MessageMultiPipeItem.STREAM_CODEC, MessageMultiPipeItem::handle);
      registerClientbound(MessagePipePayload.TYPE, MessagePipePayload.STREAM_CODEC, MessagePipePayload::handle);
      registerClientbound(MessageMultiPipePayload.TYPE, MessageMultiPipePayload.STREAM_CODEC, MessageMultiPipePayload::handle);
      registerClientboundFriendly(PayloadWireSync.TYPE, PayloadWireSync.STREAM_CODEC, PayloadWireSync::handle);
      registerClientbound(MessagePipeLandingEffect.TYPE, MessagePipeLandingEffect.STREAM_CODEC, MessagePipeLandingEffect::handle);
      registerClientboundFriendly(BuildersServerPayload.TYPE, BuildersServerPayload.STREAM_CODEC, BuildersServerPayload::handle);
   }

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

   private static <T extends CustomPacketPayload> void registerClientbound(
      Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec, BiConsumer<T, BCPayloadContext> handler
   ) {
      if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
         ClientPlayNetworking.registerGlobalReceiver(type, (payload, context) -> handler.accept((T)payload, FabricPayloadContexts.of(context.player())));
      }
   }

   private static <T extends CustomPacketPayload> void registerServerbound(
      Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec, BiConsumer<T, BCPayloadContext> handler
   ) {
      ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) -> handler.accept((T)payload, FabricPayloadContexts.of(context.player())));
   }

   private static <T extends CustomPacketPayload> void registerServerboundFriendly(
      Type<T> type, StreamCodec<? super FriendlyByteBuf, T> codec, BiConsumer<T, BCPayloadContext> handler
   ) {
      ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) -> handler.accept((T)payload, FabricPayloadContexts.of(context.player())));
   }

   private static <T extends CustomPacketPayload> void registerClientboundFriendly(
      Type<T> type, StreamCodec<? super FriendlyByteBuf, T> codec, BiConsumer<T, BCPayloadContext> handler
   ) {
      if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
         ClientPlayNetworking.registerGlobalReceiver(type, (payload, context) -> handler.accept((T)payload, FabricPayloadContexts.of(context.player())));
      }
   }
}

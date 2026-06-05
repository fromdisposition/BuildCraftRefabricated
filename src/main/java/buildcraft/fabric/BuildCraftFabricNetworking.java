package buildcraft.fabric;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import buildcraft.lib.net.MessageContainerPayload;
import buildcraft.lib.net.MessageDebugRequest;
import buildcraft.lib.net.MessageDebugResponse;
import buildcraft.fabric.network.BCPayloadContext;
import buildcraft.lib.net.MessageMarker;

import java.util.function.BiConsumer;

public final class BuildCraftFabricNetworking {
    private BuildCraftFabricNetworking() {}

    public static <T extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> void registerPlayToClient(
            net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<T> type,
            net.minecraft.network.codec.StreamCodec<? super net.minecraft.network.RegistryFriendlyByteBuf, T> codec,
            BiConsumer<T, BCPayloadContext> handler) {
        PayloadTypeRegistry.clientboundPlay().register(type, codec);
        if (net.fabricmc.loader.api.FabricLoader.getInstance().getEnvironmentType() == net.fabricmc.api.EnvType.CLIENT) {
            ClientPlayNetworking.registerGlobalReceiver(type, (payload, context) ->
                    context.client().execute(() -> handler.accept(payload, FabricPayloadContexts.of(context.player()))));
        }
    }

    public static <T extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> void registerPlayToServer(
            net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<T> type,
            net.minecraft.network.codec.StreamCodec<? super net.minecraft.network.RegistryFriendlyByteBuf, T> codec,
            BiConsumer<T, BCPayloadContext> handler) {
        PayloadTypeRegistry.serverboundPlay().register(type, codec);
        ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) ->
                context.server().execute(() -> handler.accept(payload, FabricPayloadContexts.of(context.player()))));
    }

    public static void register() {
        PayloadTypeRegistry.clientboundPlay().register(MessageMarker.TYPE, MessageMarker.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(
                buildcraft.core.marker.volume.MessageVolumeBoxes.TYPE,
                buildcraft.core.marker.volume.MessageVolumeBoxes.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(MessageDebugResponse.TYPE, MessageDebugResponse.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(
                MessageContainerPayload.TYPE, MessageContainerPayload.STREAM_CODEC);

        PayloadTypeRegistry.serverboundPlay().register(MessageDebugRequest.TYPE, MessageDebugRequest.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(
                MessageContainerPayload.TYPE, MessageContainerPayload.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(MessageDebugRequest.TYPE, (payload, context) ->
                context.server().execute(() ->
                        MessageDebugRequest.handle(payload, FabricPayloadContexts.of(context.player()))));
        ServerPlayNetworking.registerGlobalReceiver(MessageContainerPayload.TYPE, (payload, context) ->
                context.server().execute(() ->
                        MessageContainerPayload.handle(payload, FabricPayloadContexts.of(context.player()))));
    }

    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(MessageMarker.TYPE, (payload, context) ->
                context.client().execute(() ->
                        MessageMarker.handle(payload, FabricPayloadContexts.of(context.player()))));
        ClientPlayNetworking.registerGlobalReceiver(buildcraft.core.marker.volume.MessageVolumeBoxes.TYPE, (payload, context) ->
                context.client().execute(() ->
                        buildcraft.core.marker.volume.MessageVolumeBoxes.handle(payload,
                                FabricPayloadContexts.of(context.player()))));
        ClientPlayNetworking.registerGlobalReceiver(MessageDebugResponse.TYPE, (payload, context) ->
                context.client().execute(() ->
                        MessageDebugResponse.handle(payload, FabricPayloadContexts.of(context.player()))));
        ClientPlayNetworking.registerGlobalReceiver(MessageContainerPayload.TYPE, (payload, context) ->
                context.client().execute(() ->
                        MessageContainerPayload.handle(payload, FabricPayloadContexts.of(context.player()))));
    }
}

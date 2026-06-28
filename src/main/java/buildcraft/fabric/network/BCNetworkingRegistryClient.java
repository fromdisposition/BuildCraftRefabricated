/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.fabric.network;

import buildcraft.builders.snapshot.BuildersServerPayload;
import buildcraft.core.marker.volume.MessageVolumeBoxes;
import buildcraft.lib.net.MessageContainerPayload;
import buildcraft.lib.net.MessageDebugResponse;
import buildcraft.lib.net.MessageMarker;
import buildcraft.transport.net.MessageMultiPipeItem;
import buildcraft.transport.net.MessageMultiPipePayload;
import buildcraft.transport.net.MessagePipeLandingEffect;
import buildcraft.transport.net.MessagePipePayload;
import buildcraft.transport.wire.PayloadWireSync;
import java.util.function.BiConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.world.entity.player.Player;

/**
 * Clientbound packet receivers. SPLIT OUT of {@link BCNetworkingRegistry} because it references
 * {@code ClientPlayNetworking} and {@code net.minecraft.client.Minecraft}, which do not exist on a dedicated
 * server. This whole class is {@code @Environment(CLIENT)} (stripped on the server) and is only invoked from
 * {@code BuildCraftFabricClient}; the dedicated server therefore never loads any client networking type.
 * Wire codecs are registered for both sides in {@link BCNetworkingRegistry#registerCommon()}.
 */
@Environment(EnvType.CLIENT)
public final class BCNetworkingRegistryClient {
   private BCNetworkingRegistryClient() {
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

   private static <T extends CustomPacketPayload> void registerClientbound(
      Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec, BiConsumer<T, BCPayloadContext> handler
   ) {
      ClientPlayNetworking.registerGlobalReceiver(type, (payload, context) -> {
         BCNetworkingRegistry.dispatch((T) payload, clientContext(context.player()), handler);
      });
   }

   private static <T extends CustomPacketPayload> void registerClientboundFriendly(
      Type<T> type, StreamCodec<? super FriendlyByteBuf, T> codec, BiConsumer<T, BCPayloadContext> handler
   ) {
      ClientPlayNetworking.registerGlobalReceiver(type, (payload, context) -> {
         BCNetworkingRegistry.dispatch((T) payload, clientContext(context.player()), handler);
      });
   }

   /** Render-thread context: clientbound handlers run on the Minecraft client thread, replies go to the server. */
   private static BCPayloadContext clientContext(final Player player) {
      return new BCPayloadContext() {
         @Override
         public Player player() {
            return player;
         }

         @Override
         public void enqueueWork(Runnable task) {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null) {
               mc.execute(task);
            } else {
               task.run();
            }
         }

         @Override
         public void reply(CustomPacketPayload payload) {
            ClientPlayNetworking.send(payload);
         }
      };
   }
}

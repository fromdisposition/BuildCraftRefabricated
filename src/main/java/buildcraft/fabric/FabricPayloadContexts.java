package buildcraft.fabric;

import buildcraft.fabric.network.BCPayloadContext;
import buildcraft.fabric.network.PacketDistributor;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * SERVER-SAFE payload context (no client classes). Used by the serverbound handlers in
 * {@link buildcraft.fabric.network.BCNetworkingRegistry}, which is loaded on the dedicated server. The
 * clientbound counterpart that needs {@code net.minecraft.client.Minecraft} lives in the {@code @Environment}
 * -CLIENT {@code BCNetworkingRegistryClient}; nothing here may reference client-only types or the dedicated
 * server fails to load this class.
 */
public final class FabricPayloadContexts {
   private FabricPayloadContexts() {
   }

   public static BCPayloadContext of(ServerPlayer player) {
      return of((Player)player);
   }

   public static BCPayloadContext of(final Player player) {
      return new BCPayloadContext() {
         @Override
         public Player player() {
            return player;
         }

         @Override
         public void enqueueWork(Runnable task) {
            // Serverbound work runs on the integrated/dedicated server thread. (Clientbound work is scheduled
            // on the render thread by BCNetworkingRegistryClient, which is the only place allowed to touch
            // net.minecraft.client.Minecraft.)
            if (player.level().getServer() != null) {
               player.level().getServer().execute(task);
            } else {
               task.run();
            }
         }

         @Override
         public void reply(CustomPacketPayload payload) {
            if (player instanceof ServerPlayer serverPlayer) {
               PacketDistributor.sendToPlayer(serverPlayer, payload);
            }
         }
      };
   }
}

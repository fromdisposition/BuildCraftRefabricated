package buildcraft.fabric;

import buildcraft.fabric.network.BCPayloadContext;
import buildcraft.fabric.network.PacketDistributor;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

// Loaded on the dedicated server: nothing here may reference client-only types.
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

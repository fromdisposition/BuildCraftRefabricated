package buildcraft.fabric;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import buildcraft.fabric.network.BCPayloadContext;

public final class FabricPayloadContexts {
    private FabricPayloadContexts() {}

    public static BCPayloadContext of(ServerPlayer player) {
        return of((Player) player);
    }

    public static BCPayloadContext of(Player player) {
        return new BCPayloadContext() {
            @Override
            public Player player() {
                return player;
            }

            @Override
            public void enqueueWork(Runnable task) {
                if (player.level().isClientSide()) {
                    Minecraft mc = Minecraft.getInstance();
                    if (mc != null) {
                        mc.execute(task);
                    }
                } else if (player.level().getServer() != null) {
                    player.level().getServer().execute(task);
                }
            }

            @Override
            public void reply(CustomPacketPayload payload) {
                if (player instanceof ServerPlayer serverPlayer) {
                    ServerPlayNetworking.send(serverPlayer, payload);
                }
            }
        };
    }
}

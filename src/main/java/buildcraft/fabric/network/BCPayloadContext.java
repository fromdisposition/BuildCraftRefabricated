package buildcraft.fabric.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public interface BCPayloadContext {
   Player player();

   void enqueueWork(Runnable var1);

   default void reply(CustomPacketPayload payload) {
   }
}

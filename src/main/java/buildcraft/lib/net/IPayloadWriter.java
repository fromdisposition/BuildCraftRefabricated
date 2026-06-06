package buildcraft.lib.net;

import net.minecraft.network.FriendlyByteBuf;

@FunctionalInterface
public interface IPayloadWriter {
   void write(FriendlyByteBuf var1);
}

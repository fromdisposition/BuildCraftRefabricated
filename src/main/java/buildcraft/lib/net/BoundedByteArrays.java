package buildcraft.lib.net;

import java.io.IOException;

import net.minecraft.network.RegistryFriendlyByteBuf;

public final class BoundedByteArrays {
    private BoundedByteArrays() {}

    public static void write(RegistryFriendlyByteBuf buf, byte[] data) {
        if (data == null) {
            buf.writeVarInt(0);
            return;
        }
        if (data.length > BCPacketLimits.MAX_PAYLOAD_BYTES) {
            throw new IllegalArgumentException("Payload too large: " + data.length);
        }
        buf.writeVarInt(data.length);
        buf.writeBytes(data);
    }

    public static byte[] read(RegistryFriendlyByteBuf buf) throws IOException {
        int length = buf.readVarInt();
        if (length < 0 || length > BCPacketLimits.MAX_PAYLOAD_BYTES) {
            throw new IOException("Invalid payload length: " + length);
        }
        byte[] data = new byte[length];
        buf.readBytes(data);
        return data;
    }
}

package buildcraft.lib.nbt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtAccounter;

public class NbtSquisher {

    public static CompoundTag expand(InputStream stream) throws IOException {
        return NbtIo.readCompressed(stream, NbtAccounter.unlimitedHeap());
    }

    public static void squishVanilla(CompoundTag nbt, OutputStream stream) throws IOException {
        NbtIo.writeCompressed(nbt, stream);
    }
}

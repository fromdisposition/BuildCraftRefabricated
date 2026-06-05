package buildcraft.api.data;

import java.util.zip.GZIPInputStream;

import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.CompoundTag;

public class NbtSquishConstants {

    public static final int VANILLA = 0;
    public static final int VANILLA_COMPRESSED = 1;

    public static final int BUILDCRAFT_V1 = 2;
    public static final int BUILDCRAFT_V1_COMPRESSED = 3;

    public static final int BUILDCRAFT_MAGIC_1 = 0xbc;
    public static final int BUILDCRAFT_MAGIC_2 = 0xa1;

    public static final int BUILDCRAFT_MAGIC = (BUILDCRAFT_MAGIC_1 << 8) | BUILDCRAFT_MAGIC_2;

    public static final int GZIP_MAGIC_1 = GZIPInputStream.GZIP_MAGIC & 0xff;
    public static final int GZIP_MAGIC_2 = GZIPInputStream.GZIP_MAGIC >> 8;
    public static final int GZIP_MAGIC = (GZIP_MAGIC_1 << 8) | GZIP_MAGIC_2;

    public static final int FLAG_HAS_BYTES = 1 << 0;
    public static final int FLAG_HAS_SHORTS = 1 << 1;
    public static final int FLAG_HAS_INTS = 1 << 2;
    public static final int FLAG_HAS_LONGS = 1 << 3;
    public static final int FLAG_HAS_FLOATS = 1 << 4;
    public static final int FLAG_HAS_DOUBLES = 1 << 5;
    public static final int FLAG_HAS_BYTE_ARRAYS = 1 << 6;
    public static final int FLAG_HAS_INT_ARRAYS = 1 << 7;
    public static final int FLAG_HAS_STRINGS = 1 << 8;
    public static final int FLAG_HAS_COMPLEX = 1 << 9;

    public static final int COMPLEX_COMPOUND = 0;
    public static final int COMPLEX_LIST = 1;
    public static final int COMPLEX_LIST_PACKED = 2;
}

package buildcraft.lib.net;

public final class BCPacketLimits {
    /** Maximum payload bytes for container and pipe envelope byte arrays. */
    public static final int MAX_PAYLOAD_BYTES = 65536;

    /** Per-chunk size for container snapshot transfer (upload/download). */
    public static final int MAX_CHUNK_BYTES = 32 * 1024;

    /** Maximum assembled snapshot size from chunked container messages. */
    public static final int MAX_ASSEMBLED_BYTES = 4 * 1024 * 1024;

    /** Maximum compressed NBT bytes read from network payloads. */
    public static final long MAX_COMPRESSED_NBT_BYTES = 2 * 1024 * 1024;

    public static final int MAX_MARKER_POSITIONS = 8192;
    public static final int MAX_DEBUG_STRINGS = 256;
    public static final int MAX_DEBUG_STRING_LENGTH = 4096;

    public static final int MAX_WIRE_SYSTEMS = 2048;
    public static final int MAX_WIRE_ELEMENTS_PER_SYSTEM = 4096;
    public static final int MAX_WIRE_POWERED_ENTRIES = 8192;

    public static final int MAX_VOLUME_BOXES = 512;

    public static final int MAX_PIPE_ITEM_BLOCKS = 4000;
    public static final int MAX_PIPE_ITEMS_PER_BLOCK = 10;

    public static final int MAX_ARCHITECT_SCAN_POSITIONS = 500_000;

    private BCPacketLimits() {}

    public static int validateCount(int count, int max, String field) {
        if (count < 0 || count > max) {
            throw new IllegalArgumentException("Invalid " + field + " count: " + count + " (max " + max + ")");
        }
        return count;
    }

    public static void validateChunkSize(int length) {
        if (length < 0 || length > MAX_CHUNK_BYTES) {
            throw new IllegalArgumentException("Invalid chunk size: " + length);
        }
    }

    public static void validateAssembledSize(int total) {
        if (total < 0 || total > MAX_ASSEMBLED_BYTES) {
            throw new IllegalArgumentException("Assembled payload too large: " + total);
        }
    }
}

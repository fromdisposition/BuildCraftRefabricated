package buildcraft.transport;

import net.minecraft.world.level.block.entity.BlockEntityType;

import buildcraft.fabric.BCRegistries;
import buildcraft.transport.tile.TileFilteredBuffer;
import buildcraft.transport.tile.TilePipeHolder;

public final class BCTransportBlockEntities {
    public static BlockEntityType<TileFilteredBuffer> FILTERED_BUFFER;
    public static BlockEntityType<TilePipeHolder> PIPE_HOLDER;

    private BCTransportBlockEntities() {}

    public static void register() {
        FILTERED_BUFFER = BCRegistries.registerBlockEntity(BCTransport.MODID, 
                "filtered_buffer", TileFilteredBuffer::new, BCTransportBlocks.FILTERED_BUFFER.get());
        PIPE_HOLDER = BCRegistries.registerBlockEntity(BCTransport.MODID, 
                "pipe_holder", TilePipeHolder::new, BCTransportBlocks.PIPE_HOLDER.get());
    }
}

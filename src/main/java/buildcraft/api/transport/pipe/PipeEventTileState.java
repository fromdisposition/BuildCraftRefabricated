package buildcraft.api.transport.pipe;

import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class PipeEventTileState extends PipeEvent {
    PipeEventTileState(IPipeHolder holder) {
        super(holder);
    }

    public static class Invalidate extends PipeEventTileState {
        public Invalidate(IPipeHolder holder) {
            super(holder);
        }
    }

    public static class Validate extends PipeEventTileState {
        public Validate(IPipeHolder holder) {
            super(holder);
        }
    }

    public static class ChunkUnload extends PipeEventTileState {
        public ChunkUnload(IPipeHolder holder) {
            super(holder);
        }
    }
}

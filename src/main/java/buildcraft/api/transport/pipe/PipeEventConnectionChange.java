package buildcraft.api.transport.pipe;

import net.minecraft.core.Direction;

public class PipeEventConnectionChange extends PipeEvent {

    public final Direction direction;

    public PipeEventConnectionChange(IPipeHolder holder, Direction direction) {
        super(holder);
        this.direction = direction;
    }
}

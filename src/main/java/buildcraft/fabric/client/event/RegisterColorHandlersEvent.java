package buildcraft.fabric.client.event;

import java.util.List;

import net.minecraft.world.level.block.Block;

public class RegisterColorHandlersEvent {
    public static class ItemTintSources extends BCClientEvents.RegisterColorHandlersEvent.ItemTintSources {}

    public static final class BlockTintSources {
        public void register(List<?> sources, Block block) {}
    }
}


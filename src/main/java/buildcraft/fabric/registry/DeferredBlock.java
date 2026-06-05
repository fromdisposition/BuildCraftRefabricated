package buildcraft.fabric.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public class DeferredBlock<T extends net.minecraft.world.level.block.Block> extends DeferredHolder<net.minecraft.world.level.block.Block, T> {
    Identifier pendingId;

    void bind(T value, Identifier id) {
        bind(value);
        this.pendingId = id;
    }

    public Identifier getId() {
        if (pendingId != null) {
            return pendingId;
        }
        return BuiltInRegistries.BLOCK.getKey(get());
    }
}

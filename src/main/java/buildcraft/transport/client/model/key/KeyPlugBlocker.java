package buildcraft.transport.client.model.key;

import net.minecraft.core.Direction;

import buildcraft.api.transport.pluggable.PluggableModelKey;

public class KeyPlugBlocker extends PluggableModelKey {
    public KeyPlugBlocker(Direction side) {
        super("cutout", side);
    }
}

package buildcraft.api.transport.pluggable;

import java.util.Objects;

import net.minecraft.core.Direction;

public abstract class PluggableModelKey {
    public final Object layer;
    public final Direction side;
    private final int hash;

    public PluggableModelKey(Object layer, Direction side) {
        if (side == null) throw new NullPointerException("side");
        this.layer = layer;
        this.side = side;
        this.hash = Objects.hash(layer, side);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        PluggableModelKey other = (PluggableModelKey) obj;
        if (layer != other.layer) return false;
        if (side != other.side) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return hash;
    }
}

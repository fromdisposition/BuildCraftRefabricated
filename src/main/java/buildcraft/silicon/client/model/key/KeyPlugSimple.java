package buildcraft.silicon.client.model.key;

import net.minecraft.core.Direction;

import buildcraft.api.transport.pluggable.PluggableModelKey;

public class KeyPlugSimple extends PluggableModelKey {
    public final String identifier;
    public final boolean isPulsing;

    public KeyPlugSimple(String identifier, boolean isPulsing, Object layer, Direction side) {
        super(layer, side);
        this.identifier = identifier;
        this.isPulsing = isPulsing;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) return false;
        KeyPlugSimple other = (KeyPlugSimple) obj;
        if (!identifier.equals(other.identifier)) return false;
        if (isPulsing != other.isPulsing) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 31 * hash + identifier.hashCode();
        hash = 31 * hash + (isPulsing ? 1 : 0);
        return hash;
    }
}

package buildcraft.silicon.client.model.key;

import buildcraft.api.transport.pluggable.PluggableModelKey;
import net.minecraft.core.Direction;

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
      if (!super.equals(obj)) {
         return false;
      }

      KeyPlugSimple other = (KeyPlugSimple)obj;
      return !this.identifier.equals(other.identifier) ? false : this.isPulsing == other.isPulsing;
   }

   @Override
   public int hashCode() {
      int hash = super.hashCode();
      hash = 31 * hash + this.identifier.hashCode();
      return 31 * hash + (this.isPulsing ? 1 : 0);
   }
}

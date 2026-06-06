package buildcraft.silicon.client.model.key;

import buildcraft.api.transport.pluggable.PluggableModelKey;
import buildcraft.silicon.gate.GateVariant;
import net.minecraft.core.Direction;

public class KeyPlugGate extends PluggableModelKey {
   public final Direction side;
   public final GateVariant variant;
   public final boolean active;

   public KeyPlugGate(Direction side, GateVariant variant, boolean active) {
      super(null, side);
      this.side = side;
      this.variant = variant;
      this.active = active;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (obj != null && obj.getClass() == this.getClass()) {
         KeyPlugGate other = (KeyPlugGate)obj;
         return other.side == this.side && other.variant.equals(this.variant);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return this.side.hashCode() * 31 + this.variant.hashCode();
   }
}

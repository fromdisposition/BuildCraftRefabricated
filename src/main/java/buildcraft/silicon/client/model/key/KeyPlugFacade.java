package buildcraft.silicon.client.model.key;

import buildcraft.api.transport.pluggable.PluggableModelKey;
import java.util.Objects;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class KeyPlugFacade extends PluggableModelKey {
   public final BlockState state;
   public final boolean isHollow;
   private final int hash;

   public KeyPlugFacade(Object layer, Direction side, BlockState state, boolean isHollow) {
      super(layer, side);
      this.state = state;
      this.isHollow = isHollow;
      this.hash = Objects.hash(layer, side, state, isHollow);
   }

   @Override
   public int hashCode() {
      return this.hash;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      }

      if (obj == null) {
         return false;
      }

      if (obj.getClass() != this.getClass()) {
         return false;
      }

      KeyPlugFacade other = (KeyPlugFacade)obj;
      return other.isHollow == this.isHollow && other.layer == this.layer && other.state == this.state && other.side == this.side;
   }
}

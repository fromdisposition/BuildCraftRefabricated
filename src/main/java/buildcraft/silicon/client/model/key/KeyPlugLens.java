package buildcraft.silicon.client.model.key;

import buildcraft.api.transport.pluggable.PluggableModelKey;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;

public class KeyPlugLens extends PluggableModelKey {
   @Nullable
   public final DyeColor colour;
   public final boolean isFilter;
   private final int hash;

   public KeyPlugLens(Object layer, Direction side, @Nullable DyeColor colour, boolean isFilter) {
      super(layer, side);
      this.colour = colour;
      this.isFilter = isFilter;
      this.hash = Objects.hash(layer, side, colour, isFilter);
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

      KeyPlugLens other = (KeyPlugLens)obj;
      return other.isFilter == this.isFilter && other.layer == this.layer && other.colour == this.colour && other.side == this.side;
   }
}

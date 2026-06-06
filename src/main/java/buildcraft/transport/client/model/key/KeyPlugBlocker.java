package buildcraft.transport.client.model.key;

import buildcraft.api.transport.pluggable.PluggableModelKey;
import net.minecraft.core.Direction;

public class KeyPlugBlocker extends PluggableModelKey {
   public KeyPlugBlocker(Direction side) {
      super("cutout", side);
   }
}

package buildcraft.transport.client.model.key;

import buildcraft.api.transport.pluggable.PluggableModelKey;
import net.minecraft.core.Direction;

public class KeyPlugPowerAdaptor extends PluggableModelKey {
   public KeyPlugPowerAdaptor(Direction side) {
      super("cutout", side);
   }
}

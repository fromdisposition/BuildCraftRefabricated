package buildcraft.api.power;

import net.minecraft.core.Direction;

public interface IEngine {
   boolean canReceiveFromEngine(Direction var1);

   boolean receivePower(long var1, boolean var3);
}

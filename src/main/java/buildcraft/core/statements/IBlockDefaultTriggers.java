package buildcraft.core.statements;

import net.minecraft.core.Direction;

public interface IBlockDefaultTriggers {
   boolean blockInventoryTriggers(Direction var1);

   boolean blockFluidHandlerTriggers(Direction var1);
}

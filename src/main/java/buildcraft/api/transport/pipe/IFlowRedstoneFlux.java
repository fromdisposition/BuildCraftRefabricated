package buildcraft.api.transport.pipe;

import net.minecraft.core.Direction;

public interface IFlowRedstoneFlux extends IFlowPowerLike {
   @Override
   void reconfigure();

   int tryExtractPower(int var1, Direction var2);
}

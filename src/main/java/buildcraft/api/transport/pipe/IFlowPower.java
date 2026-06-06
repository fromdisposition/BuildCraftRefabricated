package buildcraft.api.transport.pipe;

import net.minecraft.core.Direction;

public interface IFlowPower extends IFlowPowerLike {
   @Override
   void reconfigure();

   long tryExtractPower(long var1, Direction var3);
}

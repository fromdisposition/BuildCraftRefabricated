package buildcraft.api.transport.pipe;

import net.minecraft.core.Direction;

import buildcraft.api.mj.IMjPassiveProvider;

public interface IFlowPower extends IFlowPowerLike {

    @Override
    void reconfigure();

    long tryExtractPower(long maxPower, Direction from);
}

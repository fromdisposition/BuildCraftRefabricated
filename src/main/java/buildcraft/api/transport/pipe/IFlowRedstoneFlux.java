package buildcraft.api.transport.pipe;

import net.minecraft.core.Direction;

import buildcraft.lib.transfer.energy.EnergyHandler;

public interface IFlowRedstoneFlux extends IFlowPowerLike {

    @Override
    void reconfigure();

    int tryExtractPower(int maxPower, Direction from);
}

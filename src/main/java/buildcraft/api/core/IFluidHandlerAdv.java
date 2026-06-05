package buildcraft.api.core;

import javax.annotation.Nullable;

import buildcraft.lib.transfer.ResourceHandler;
import buildcraft.lib.transfer.fluid.FluidResource;
import buildcraft.lib.transfer.transaction.TransactionContext;

public interface IFluidHandlerAdv extends ResourceHandler<FluidResource> {

    int extract(IFluidFilter filter, int maxDrain, TransactionContext tx);
}

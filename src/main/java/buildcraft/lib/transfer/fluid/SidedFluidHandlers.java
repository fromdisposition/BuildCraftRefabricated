package buildcraft.lib.transfer.fluid;

import buildcraft.lib.transfer.ResourceHandler;
import buildcraft.lib.transfer.transaction.TransactionContext;

/** One-sided fluid tank wrappers for machine faces. */
public final class SidedFluidHandlers {
    private SidedFluidHandlers() {}

    public static ResourceHandler<FluidResource> insertOnly(ResourceHandler<FluidResource> tank) {
        return new ResourceHandler<>() {
            @Override public int size() { return tank.size(); }
            @Override public FluidResource getResource(int index) { return tank.getResource(index); }
            @Override public long getAmountAsLong(int index) { return tank.getAmountAsLong(index); }
            @Override public long getCapacityAsLong(int index, FluidResource resource) {
                return tank.getCapacityAsLong(index, resource);
            }
            @Override public boolean isValid(int index, FluidResource resource) {
                return tank.isValid(index, resource);
            }
            @Override public int insert(int index, FluidResource resource, int amount, TransactionContext tx) {
                return tank.insert(index, resource, amount, tx);
            }
            @Override public int extract(int index, FluidResource resource, int amount, TransactionContext tx) {
                return 0;
            }
        };
    }

    public static ResourceHandler<FluidResource> extractOnly(ResourceHandler<FluidResource> tank) {
        return new ResourceHandler<>() {
            @Override public int size() { return tank.size(); }
            @Override public FluidResource getResource(int index) { return tank.getResource(index); }
            @Override public long getAmountAsLong(int index) { return tank.getAmountAsLong(index); }
            @Override public long getCapacityAsLong(int index, FluidResource resource) {
                return tank.getCapacityAsLong(index, resource);
            }
            @Override public boolean isValid(int index, FluidResource resource) {
                return tank.isValid(index, resource);
            }
            @Override public int extract(int index, FluidResource resource, int amount, TransactionContext tx) {
                return tank.extract(index, resource, amount, tx);
            }
            @Override public int insert(int index, FluidResource resource, int amount, TransactionContext tx) {
                return 0;
            }
        };
    }
}

package buildcraft.api.transport.pipe;

import javax.annotation.Nullable;

import net.minecraft.core.Direction;

import buildcraft.lib.fluids.FluidStack;

import buildcraft.api.core.IFluidFilter;

public interface IFlowFluid {

    @Nullable
    @Deprecated
    default FluidStack tryExtractFluid(int millibuckets, Direction from, FluidStack filter) {
        return tryExtractFluid(millibuckets, from, filter, false);
    }

    @Nullable
    FluidStack tryExtractFluid(int millibuckets, Direction from, FluidStack filter, boolean simulate);

    @Deprecated
    default Object tryExtractFluidAdv(int millibuckets, Direction from, IFluidFilter filter) {
        return tryExtractFluidAdv(millibuckets, from, filter, false);
    }

    Object tryExtractFluidAdv(int millibuckets, Direction from, IFluidFilter filter, boolean simulate);

    int insertFluidsForce(FluidStack fluid, @Nullable Direction from, boolean simulate);

    @Nullable
    FluidStack extractFluidsForce(int min, int max, @Nullable Direction section, boolean simulate);
}

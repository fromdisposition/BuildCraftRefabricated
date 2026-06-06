package buildcraft.api.fuels;

import buildcraft.lib.fluids.FluidStack;

public interface IFuel {
   FluidStack getFluid();

   int getTotalBurningTime();

   long getPowerPerCycle();
}

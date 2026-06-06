package buildcraft.api.fuels;

import buildcraft.lib.fluids.FluidStack;

public interface ICoolant {
   boolean matchesFluid(FluidStack var1);

   float getDegreesCoolingPerMB(FluidStack var1, float var2);

   default FluidStack getRepresentativeFluid() {
      return FluidStack.EMPTY;
   }
}

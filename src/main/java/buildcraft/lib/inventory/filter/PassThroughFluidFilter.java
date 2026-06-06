package buildcraft.lib.inventory.filter;

import buildcraft.api.core.IFluidFilter;
import buildcraft.lib.fluids.FluidStack;

public class PassThroughFluidFilter implements IFluidFilter {
   @Override
   public boolean matches(FluidStack fluid) {
      return fluid != null;
   }
}

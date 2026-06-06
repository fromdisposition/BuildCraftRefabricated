package buildcraft.lib.inventory.filter;

import buildcraft.api.core.IFluidFilter;
import buildcraft.lib.fluids.FluidStack;

public class InvertedFluidFilter implements IFluidFilter {
   public final IFluidFilter delegate;

   public InvertedFluidFilter(IFluidFilter delegate) {
      this.delegate = delegate;
   }

   @Override
   public boolean matches(FluidStack fluid) {
      return !this.delegate.matches(fluid);
   }
}

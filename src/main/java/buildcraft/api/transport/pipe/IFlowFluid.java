package buildcraft.api.transport.pipe;

import buildcraft.api.core.IFluidFilter;
import buildcraft.lib.fluids.FluidStack;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;

public interface IFlowFluid {
   @Nullable
   @Deprecated
   default FluidStack tryExtractFluid(int millibuckets, Direction from, FluidStack filter) {
      return this.tryExtractFluid(millibuckets, from, filter, false);
   }

   @Nullable
   FluidStack tryExtractFluid(int var1, Direction var2, FluidStack var3, boolean var4);

   @Deprecated
   default Object tryExtractFluidAdv(int millibuckets, Direction from, IFluidFilter filter) {
      return this.tryExtractFluidAdv(millibuckets, from, filter, false);
   }

   Object tryExtractFluidAdv(int var1, Direction var2, IFluidFilter var3, boolean var4);

   int insertFluidsForce(FluidStack var1, @Nullable Direction var2, boolean var3);

   @Nullable
   FluidStack extractFluidsForce(int var1, int var2, @Nullable Direction var3, boolean var4);
}

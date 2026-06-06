package buildcraft.lib.client.guide.entry;

import buildcraft.lib.fluids.FluidStack;
import java.util.Objects;
import net.minecraft.world.level.material.Fluid;

public class FluidStackValueFilter {
   public final FluidStack stack;

   public FluidStackValueFilter(FluidStack stack) {
      this.stack = stack;
   }

   public FluidStackValueFilter(Fluid fluid) {
      this(new FluidStack(fluid, 1));
   }

   public Fluid getFluid() {
      return this.stack.getFluid();
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (obj != null && obj.getClass() == this.getClass()) {
         FluidStackValueFilter other = (FluidStackValueFilter)obj;
         return this.stack.getFluid() == other.stack.getFluid();
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.stack.getFluid());
   }

   @Override
   public String toString() {
      return "FluidStackValueFilter[" + this.stack.getFluid() + "]";
   }
}

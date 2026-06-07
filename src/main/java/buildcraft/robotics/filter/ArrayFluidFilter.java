package buildcraft.robotics.filter;

import buildcraft.lib.fluids.FluidStack;
import buildcraft.robotics.path.IFluidFilter;
import buildcraft.transport.pipe.behaviour.FilterFluidStacks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

/** Matches fluids derived from configured item stacks (buckets, fluid containers). */
public class ArrayFluidFilter implements IFluidFilter {
   private final Fluid[] fluids;

   public ArrayFluidFilter(ItemStack... stacks) {
      this.fluids = new Fluid[stacks.length];

      for (int i = 0; i < stacks.length; i++) {
         FluidStack fluid = FilterFluidStacks.fluidFromFilter(stacks[i]);
         if (!fluid.isEmpty()) {
            this.fluids[i] = fluid.getFluid();
         }
      }
   }

   public boolean hasFilter() {
      for (Fluid fluid : this.fluids) {
         if (fluid != null) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean matches(Fluid fluid) {
      if (fluid == null) {
         return false;
      }

      if (!this.hasFilter()) {
         return true;
      }

      for (Fluid filter : this.fluids) {
         if (filter != null && filter.isSame(fluid)) {
            return true;
         }
      }

      return false;
   }
}

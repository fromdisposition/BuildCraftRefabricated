/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.entry;

import buildcraft.lib.fluid.stack.FluidStack;
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

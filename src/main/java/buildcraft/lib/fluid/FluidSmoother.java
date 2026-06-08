/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.fluid;

import buildcraft.lib.fabric.transfer.SingleFluidTank;
import buildcraft.lib.fluids.FluidStack;
import java.util.List;
import net.minecraft.core.Direction;

public class FluidSmoother {
   private final SingleFluidTank tank;
   private double displayAmount;
   private double displayAmountPrev;
   private boolean initialized = false;

   public FluidSmoother(SingleFluidTank tank) {
      this.tank = tank;
   }

   public void tick() {
      int target = this.tank.getAmountMb();
      if (!this.initialized) {
         this.displayAmount = target;
         this.displayAmountPrev = target;
         this.initialized = true;
      } else {
         this.displayAmountPrev = this.displayAmount;
         if (this.displayAmount != target) {
            double delta = target - this.displayAmount;
            double step = Math.max(1.0, Math.abs(delta) * 0.2);
            if (Math.abs(delta) <= step) {
               this.displayAmount = target;
            } else {
               this.displayAmount = this.displayAmount + Math.signum(delta) * step;
            }
         }
      }
   }

   public void resetSmoothing() {
      this.displayAmount = this.tank.getAmountMb();
      this.displayAmountPrev = this.displayAmount;
      this.initialized = true;
   }

   public double getAmount(double partialTicks) {
      return this.displayAmountPrev + (this.displayAmount - this.displayAmountPrev) * partialTicks;
   }

   public FluidStack getFluid() {
      return this.tank.getFluidStack();
   }

   public int getCapacity() {
      return this.tank.getCapacityMb();
   }

   public double getDisplayAmount() {
      return this.displayAmount;
   }

   public void getDebugInfo(List<String> left, List<String> right, Direction side) {
      String contents = !this.tank.isEmpty() ? "Fluid" : "Empty";
      left.add("smooth = " + String.format("%.1f", this.displayAmount) + " / " + this.tank.getAmountMb() + " (" + contents + ")");
   }

   public FluidSmoother.FluidStackInterp getFluidForRender(double partialTicks) {
      FluidStack fluid = this.tank.getFluidStack();
      return fluid.isEmpty() ? null : new FluidSmoother.FluidStackInterp(fluid, this.getAmount(partialTicks));
   }

   public record FluidStackInterp(FluidStack fluid, double amount) {
   }
}

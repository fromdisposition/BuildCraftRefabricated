/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.fluid.registry;

import buildcraft.lib.fluid.stack.FluidStack;
import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import net.minecraft.core.Direction;

public class FluidSmoother {
   private final IntSupplier amountMbSupplier;
   private final Supplier<FluidStack> fluidSupplier;
   private final IntSupplier capacityMbSupplier;
   private double displayAmount;
   private double displayAmountPrev;
   private boolean initialized = false;

   public FluidSmoother(IntSupplier amountMbSupplier, Supplier<FluidStack> fluidSupplier, IntSupplier capacityMbSupplier) {
      this.amountMbSupplier = amountMbSupplier;
      this.fluidSupplier = fluidSupplier;
      this.capacityMbSupplier = capacityMbSupplier;
   }

   public void tick() {
      int target = this.amountMbSupplier.getAsInt();
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
      this.displayAmount = this.amountMbSupplier.getAsInt();
      this.displayAmountPrev = this.displayAmount;
      this.initialized = true;
   }

   public double getAmount(double partialTicks) {
      return this.displayAmountPrev + (this.displayAmount - this.displayAmountPrev) * partialTicks;
   }

   public FluidStack getFluid() {
      return this.fluidSupplier.get();
   }

   public int getCapacity() {
      return this.capacityMbSupplier.getAsInt();
   }

   public double getDisplayAmount() {
      return this.displayAmount;
   }

   public void getDebugInfo(List<String> left, List<String> right, Direction side) {
      FluidStack fluid = this.fluidSupplier.get();
      String contents = !fluid.isEmpty() ? "Fluid" : "Empty";
      left.add("smooth = " + String.format("%.1f", this.displayAmount) + " / " + this.amountMbSupplier.getAsInt() + " (" + contents + ")");
   }

   public FluidSmoother.FluidStackInterp getFluidForRender(double partialTicks) {
      FluidStack fluid = this.fluidSupplier.get();
      return fluid.isEmpty() ? null : new FluidSmoother.FluidStackInterp(fluid, this.getAmount(partialTicks));
   }

   public record FluidStackInterp(FluidStack fluid, double amount) {
   }
}

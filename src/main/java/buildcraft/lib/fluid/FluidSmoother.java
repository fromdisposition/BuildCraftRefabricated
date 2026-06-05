/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.fluid;

import buildcraft.lib.fluids.FluidStack;
import buildcraft.lib.transfer.fluid.FluidResource;
import buildcraft.lib.transfer.fluid.FluidStacksResourceHandler;

public class FluidSmoother {

    private final FluidStacksResourceHandler tank;

    private double displayAmount;

    private double displayAmountPrev;

    private boolean initialized = false;

    public FluidSmoother(FluidStacksResourceHandler tank) {
        this.tank = tank;
    }

    public void tick() {
        int target = tank.getAmountAsInt(0);

        if (!initialized) {
            displayAmount = target;
            displayAmountPrev = target;
            initialized = true;
            return;
        }

        displayAmountPrev = displayAmount;

        if (displayAmount != target) {
            double delta = target - displayAmount;

            double step = Math.max(1.0, Math.abs(delta) * 0.2);
            if (Math.abs(delta) <= step) {
                displayAmount = target;
            } else {
                displayAmount += Math.signum(delta) * step;
            }
        }
    }

    public void resetSmoothing() {
        displayAmount = tank.getAmountAsInt(0);
        displayAmountPrev = displayAmount;
        initialized = true;
    }

    public double getAmount(double partialTicks) {
        return displayAmountPrev + (displayAmount - displayAmountPrev) * partialTicks;
    }

    public FluidStack getFluid() {
        FluidResource res = tank.getResource(0);
        return res.isEmpty() ? FluidStack.EMPTY : res.toStack(tank.getAmountAsInt(0));
    }

    public int getCapacity() {
        return tank.getCapacityAsInt(0, FluidResource.EMPTY);
    }

    public double getDisplayAmount() {
        return displayAmount;
    }

    public void getDebugInfo(java.util.List<String> left, java.util.List<String> right, net.minecraft.core.Direction side) {
        String contents = (!tank.getResource(0).isEmpty()) ? "Fluid" : "Empty";
        left.add("smooth = " + String.format("%.1f", displayAmount) + " / " + target() + " (" + contents + ")");
    }

    private int target() {
        return tank.getAmountAsInt(0);
    }

    public record FluidStackInterp(FluidStack fluid, double amount) {}

    public FluidStackInterp getFluidForRender(double partialTicks) {
        FluidResource res = tank.getResource(0);
        if (res.isEmpty()) {
            return null;
        }
        return new FluidStackInterp(res.toStack(tank.getAmountAsInt(0)), getAmount(partialTicks));
    }
}

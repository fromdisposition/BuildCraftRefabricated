/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.lib.fluid;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.world.item.ItemStack;

import buildcraft.lib.fluids.FluidStack;

import buildcraft.api.fuels.ICoolant;
import buildcraft.api.fuels.ICoolantManager;
import buildcraft.api.fuels.ISolidCoolant;

public enum CoolantRegistry implements ICoolantManager {
    INSTANCE;

    private final List<ICoolant> coolants = new LinkedList<>();
    private final List<ISolidCoolant> solidCoolants = new LinkedList<>();

    @Override
    public ICoolant addCoolant(ICoolant coolant) {
        coolants.add(coolant);
        return coolant;
    }

    @Override
    public ISolidCoolant addSolidCoolant(ISolidCoolant solidCoolant) {
        solidCoolants.add(solidCoolant);
        return solidCoolant;
    }

    @Override
    public ICoolant addCoolant(FluidStack fluid, float degreesCoolingPerMb) {
        return addCoolant(new Coolant(fluid, degreesCoolingPerMb));
    }

    @Override
    public ISolidCoolant addSolidCoolant(ItemStack solid, FluidStack fluid, float multiplier) {
        return addSolidCoolant(new SolidCoolant(solid, fluid, multiplier));
    }

    @Override
    public Collection<ICoolant> getCoolants() {
        return coolants;
    }

    @Override
    public Collection<ISolidCoolant> getSolidCoolants() {
        return solidCoolants;
    }

    @Override
    public ICoolant getCoolant(FluidStack fluid) {
        if (fluid == null || fluid.isEmpty()) {
            return null;
        }
        for (ICoolant coolant : coolants) {
            if (coolant.matchesFluid(fluid)) {
                return coolant;
            }
        }
        return null;
    }

    @Override
    public float getDegreesPerMb(FluidStack fluid, float heat) {
        if (fluid == null || fluid.isEmpty()) {
            return 0;
        }
        for (ICoolant coolant : coolants) {
            float degrees = coolant.getDegreesCoolingPerMB(fluid, heat);
            if (degrees > 0) {
                return degrees;
            }
        }
        return 0;
    }

    @Override
    public ISolidCoolant getSolidCoolant(ItemStack solid) {
        for (ISolidCoolant coolant : solidCoolants) {
            if (coolant.getFluidFromSolidCoolant(solid) != null) {
                return coolant;
            }
        }
        return null;
    }

    public static class Coolant implements ICoolant {
        private final FluidStack fluid;
        private final float degreesCoolingPerMb;

        public Coolant(FluidStack fluid, float degreesCoolingPerMb) {
            this.fluid = fluid;
            this.degreesCoolingPerMb = degreesCoolingPerMb;
        }

        @Override
        public boolean matchesFluid(FluidStack stack) {
            return FluidStack.isSameFluid(fluid, stack);
        }

        @Override
        public float getDegreesCoolingPerMB(FluidStack stack, float heat) {
            if (matchesFluid(stack)) {
                return degreesCoolingPerMb;
            }
            return 0;
        }

        @Override
        public FluidStack getRepresentativeFluid() {
            return fluid;
        }
    }

    public static class SolidCoolant implements ISolidCoolant {
        private final ItemStack solid;
        private final FluidStack fluid;
        private final float multiplier;

        public SolidCoolant(ItemStack solid, FluidStack fluid, float multiplier) {
            this.solid = solid;
            this.fluid = fluid;
            this.multiplier = multiplier;
        }

        @Override
        public FluidStack getFluidFromSolidCoolant(ItemStack stack) {
            if (stack == null || !ItemStack.isSameItem(stack, solid)) {
                return null;
            }
            int liquidAmount = (int) (stack.getCount() * fluid.getAmount() * multiplier / solid.getCount());
            return new FluidStack(fluid.getFluid(), liquidAmount);
        }

        @Override
        public ItemStack getRepresentativeStack() {
            return solid;
        }
    }
}

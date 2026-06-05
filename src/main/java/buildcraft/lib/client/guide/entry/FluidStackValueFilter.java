package buildcraft.lib.client.guide.entry;

import java.util.Objects;

import net.minecraft.world.level.material.Fluid;

import buildcraft.lib.fluids.FluidStack;

public class FluidStackValueFilter {
    public final FluidStack stack;

    public FluidStackValueFilter(FluidStack stack) {
        this.stack = stack;
    }

    public FluidStackValueFilter(Fluid fluid) {
        this(new FluidStack(fluid, 1));
    }

    public Fluid getFluid() {
        return stack.getFluid();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != getClass()) return false;
        FluidStackValueFilter other = (FluidStackValueFilter) obj;
        return stack.getFluid() == other.stack.getFluid();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(stack.getFluid());
    }

    @Override
    public String toString() {
        return "FluidStackValueFilter[" + stack.getFluid() + "]";
    }
}

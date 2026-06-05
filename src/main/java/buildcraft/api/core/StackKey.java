/* Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. */
package buildcraft.api.core;

import java.util.Objects;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import buildcraft.lib.fluids.FluidStack;

public final class StackKey {
    public final ItemStack stack;
    public final FluidStack fluidStack;

    private StackKey(ItemStack stack, FluidStack fluidStack) {
        this.stack = stack;
        this.fluidStack = fluidStack;
    }

    private StackKey(ItemStack stack) {
        this(stack, null);
    }

    private StackKey(FluidStack stack) {
        this(null, stack);
    }

    public static StackKey stack(Item item, int amount, int damage) {
        return new StackKey(new ItemStack(item, amount));
    }

    public static StackKey stack(Block block, int amount, int damage) {
        return new StackKey(new ItemStack(block, amount));
    }

    public static StackKey stack(Item item) {
        return new StackKey(new ItemStack(item, 1));
    }

    public static StackKey stack(Block block) {
        return new StackKey(new ItemStack(block, 1));
    }

    public static StackKey fluid(FluidStack fluidStack) {
        return new StackKey(fluidStack);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof StackKey))
            return false;
        StackKey k = (StackKey) obj;
        if ((stack == null) != (k.stack == null) || (fluidStack == null) != (k.fluidStack == null)) {
            return false;
        }
        if (stack != null) {
            if (!ItemStack.isSameItem(stack, k.stack)) {
                return false;
            }
        }
        if (fluidStack != null) {
            if (!FluidStack.isSameFluidSameComponents(fluidStack, k.fluidStack)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 1;
        if (stack != null) {
            result = 31 * result + stack.getItem().hashCode();
        }
        if (fluidStack != null) {
            result = 31 * result + fluidStack.getFluid().hashCode();
            result = 31 * result + fluidStack.getAmount();
        }
        return result;
    }

    @Override
    public String toString() {
        if (stack != null)
            return "StackKey{item=" + stack + "}";
        if (fluidStack != null)
            return "StackKey{fluid=" + fluidStack + "}";
        return "StackKey{empty}";
    }
}

package buildcraft.api.items;

import javax.annotation.Nullable;

import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;

import buildcraft.lib.fluids.FluidStack;

public interface IItemFluidShard {
    void addFluidDrops(NonNullList<ItemStack> toDrop, @Nullable FluidStack fluid);
}

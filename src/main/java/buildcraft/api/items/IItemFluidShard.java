package buildcraft.api.items;

import buildcraft.lib.fluids.FluidStack;
import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

public interface IItemFluidShard {
   void addFluidDrops(NonNullList<ItemStack> var1, @Nullable FluidStack var2);
}

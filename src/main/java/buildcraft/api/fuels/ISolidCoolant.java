package buildcraft.api.fuels;

import buildcraft.lib.fluids.FluidStack;
import net.minecraft.world.item.ItemStack;

public interface ISolidCoolant {
   FluidStack getFluidFromSolidCoolant(ItemStack var1);

   default ItemStack getRepresentativeStack() {
      return ItemStack.EMPTY;
   }
}

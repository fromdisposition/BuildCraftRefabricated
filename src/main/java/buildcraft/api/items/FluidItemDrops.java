package buildcraft.api.items;

import buildcraft.lib.fabric.transfer.SingleFluidTank;
import buildcraft.lib.fluids.FluidStack;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

public class FluidItemDrops {
   public static IItemFluidShard item;

   public static void addFluidDrops(NonNullList<ItemStack> toDrop, FluidStack... fluids) {
      if (item != null) {
         for (FluidStack fluid : fluids) {
            item.addFluidDrops(toDrop, fluid);
         }
      }
   }

   @SafeVarargs
   public static void addFluidDrops(NonNullList<ItemStack> toDrop, SingleFluidTank... tanks) {
      if (item != null) {
         for (SingleFluidTank tank : tanks) {
            if (tank != null && !tank.isEmpty()) {
               item.addFluidDrops(toDrop, tank.getFluidStack());
            }
         }
      }
   }
}

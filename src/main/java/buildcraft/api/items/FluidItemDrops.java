package buildcraft.api.items;

import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;

import buildcraft.lib.fluids.FluidStack;
import buildcraft.lib.fluids.IFluidTank;

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
    public static void addFluidDrops(NonNullList<ItemStack> toDrop, buildcraft.lib.transfer.ResourceHandler<buildcraft.lib.transfer.fluid.FluidResource>... tanks) {
        if (item != null) {
            for (buildcraft.lib.transfer.ResourceHandler<buildcraft.lib.transfer.fluid.FluidResource> tank : tanks) {
                if (tank != null && tank.size() > 0) {
                    buildcraft.lib.transfer.fluid.FluidResource res = tank.getResource(0);
                    if (!res.isEmpty()) {
                        item.addFluidDrops(toDrop, res.toStack((int) tank.getAmountAsLong(0)));
                    }
                }
            }
        }
    }
}

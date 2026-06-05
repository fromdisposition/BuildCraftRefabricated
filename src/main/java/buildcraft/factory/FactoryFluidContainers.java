package buildcraft.factory;

import net.minecraft.world.item.ItemStack;

import buildcraft.lib.fluids.FluidConstants;

import buildcraft.lib.tile.item.ItemHandlerSimple;

import buildcraft.lib.transfer.ResourceHandlerUtil;

import buildcraft.lib.transfer.access.ItemAccess;

import buildcraft.lib.transfer.fluid.BucketResourceHandler;

import buildcraft.lib.transfer.fluid.FluidResource;

import buildcraft.lib.transfer.fluid.FluidStacksResourceHandler;

import buildcraft.lib.transfer.transaction.Transaction;

public final class FactoryFluidContainers {

    private FactoryFluidContainers() {}

    public static boolean tryDrainBucketIntoTank(ItemStack stack, FluidStacksResourceHandler tank) {

        if (stack.isEmpty() || stack.getCount() != 1) {

            return false;

        }

        BucketResourceHandler from = new BucketResourceHandler(ItemAccess.forStack(stack));

        try (var tx = Transaction.openRoot()) {

            int moved = ResourceHandlerUtil.move(from, tank, r -> !r.isEmpty(), FluidConstants.BUCKET_VOLUME, tx);

            if (moved > 0) {

                tx.commit();

                return true;

            }

        }

        return false;

    }

    public static boolean tryFillBucketFromTank(ItemStack stack, FluidStacksResourceHandler tank) {

        if (stack.isEmpty() || stack.getCount() != 1) {

            return false;

        }

        BucketResourceHandler to = new BucketResourceHandler(ItemAccess.forStack(stack));

        try (var tx = Transaction.openRoot()) {

            int moved = ResourceHandlerUtil.move(tank, to, r -> !r.isEmpty(), FluidConstants.BUCKET_VOLUME, tx);

            if (moved > 0) {

                tx.commit();

                return true;

            }

        }

        return false;

    }

    public static void syncDrainSlot(ItemHandlerSimple slots, int slot, FluidStacksResourceHandler tank) {

        ItemStack stack = slots.getStackInSlot(slot);

        if (tryDrainBucketIntoTank(stack, tank)) {

            slots.setStackInSlot(slot, stack);

        }

    }

    public static void syncFillSlot(ItemHandlerSimple slots, int slot, FluidStacksResourceHandler tank) {

        ItemStack stack = slots.getStackInSlot(slot);

        if (tryFillBucketFromTank(stack, tank)) {

            slots.setStackInSlot(slot, stack);

        }

    }

}

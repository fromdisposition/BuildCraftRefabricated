/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package buildcraft.lib.transfer.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import buildcraft.lib.attachments.Attachments;
import buildcraft.lib.transfer.access.ItemAccess;
import buildcraft.lib.transfer.item.ItemResource;
import buildcraft.lib.transfer.item.ItemStacksResourceHandler;
import buildcraft.lib.transfer.item.ItemUtil;

public final class DispenseFluidContainer extends DefaultDispenseItemBehavior {
    private static final DispenseFluidContainer INSTANCE = new DispenseFluidContainer();

    public static DispenseFluidContainer getInstance() {
        return INSTANCE;
    }

    private DispenseFluidContainer() {}

    @Override
    public ItemStack execute(BlockSource source, ItemStack stack) {

        var containingHandler = new ItemStacksResourceHandler(2);
        containingHandler.set(0, ItemResource.of(stack), stack.getCount());
        var itemAccess = ItemAccess.forHandlerIndex(containingHandler, 0).oneByOne();

        var resourceHandler = itemAccess.getCapability(Attachments.Fluid.ITEM);
        if (resourceHandler == null) {
            return super.execute(source, stack);
        }

        Direction dispenserFacing = source.state().getValue(DispenserBlock.FACING);
        BlockPos targetPos = source.pos().relative(dispenserFacing);

        if (!FluidUtil.tryPickupFluid(resourceHandler, null, source.level(), targetPos, dispenserFacing.getOpposite()).isEmpty()
                || !FluidUtil.tryPlaceFluid(resourceHandler, null, source.level(), InteractionHand.MAIN_HAND, targetPos).isEmpty()) {
            var stack0 = ItemUtil.getStack(containingHandler, 0);
            var stack1 = ItemUtil.getStack(containingHandler, 1);

            stack0.grow(1);
            return this.consumeWithRemainder(source, stack, stack1);
        } else {
            return super.execute(source, stack);
        }
    }
}

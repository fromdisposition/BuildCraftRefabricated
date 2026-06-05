/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.lib.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import buildcraft.lib.transfer.ResourceHandler;
import buildcraft.lib.transfer.fluid.FluidResource;

import buildcraft.api.items.FluidItemDrops;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.lib.tile.item.ItemHandlerSimple;

public final class BlockDropsUtil {
    private BlockDropsUtil() {}

    @SafeVarargs
    public static void dropTileContents(Level level, BlockPos pos, TileBC_Neptune tile,
            ResourceHandler<FluidResource>... fluidTanks) {
        if (level.isClientSide()) {
            return;
        }
        NonNullList<ItemStack> toDrop = NonNullList.create();
        tile.addDrops(toDrop, 0);
        if (fluidTanks != null && fluidTanks.length > 0) {
            FluidItemDrops.addFluidDrops(toDrop, fluidTanks);
        }
        for (ItemStack drop : toDrop) {
            if (!drop.isEmpty()) {
                Block.popResource(level, pos, drop);
            }
        }
    }

    public static void dropItems(Level level, BlockPos pos, ItemHandlerSimple... handlers) {
        if (level.isClientSide() || handlers == null) {
            return;
        }
        for (ItemHandlerSimple handler : handlers) {
            if (handler == null) continue;
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                ItemStack stack = handler.getStackInSlot(slot);
                if (!stack.isEmpty()) {
                    Block.popResource(level, pos, stack);
                }
            }
        }
    }

    public static void dropStack(Level level, BlockPos pos, ItemStack stack) {
        if (!level.isClientSide() && stack != null && !stack.isEmpty()) {
            Block.popResource(level, pos, stack);
        }
    }

    @SafeVarargs
    public static void dropFluidShards(Level level, BlockPos pos,
            ResourceHandler<FluidResource>... tanks) {
        if (level.isClientSide() || tanks == null || tanks.length == 0) {
            return;
        }
        NonNullList<ItemStack> toDrop = NonNullList.create();
        FluidItemDrops.addFluidDrops(toDrop, tanks);
        for (ItemStack drop : toDrop) {
            if (!drop.isEmpty()) {
                Block.popResource(level, pos, drop);
            }
        }
    }
}

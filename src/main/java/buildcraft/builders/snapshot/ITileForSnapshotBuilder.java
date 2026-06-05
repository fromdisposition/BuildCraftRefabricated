/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import buildcraft.lib.fluids.FluidStack;

import buildcraft.api.core.IPlayerOwned;
import buildcraft.api.mj.MjBattery;

public interface ITileForSnapshotBuilder extends IPlayerOwned {
    Level getWorldBC();

    MjBattery getBattery();

    BlockPos getBuilderPos();

    boolean canExcavate();

    SnapshotBuilder<?> getBuilder();

    default EnumFluidHandlingMode getFluidMode() {
        return EnumFluidHandlingMode.NO_REPLACE;
    }

    default EnumContainerContentsMode getContainerContentsMode() {
        return EnumContainerContentsMode.INCLUDE;
    }

    default ItemStack getBreakingTool() {
        return new ItemStack(Items.DIAMOND_PICKAXE);
    }

    default void onBlockBroken(BlockPos brokenPos, List<ItemStack> drops, int xp, FluidStack capturedFluid) {
    }
}

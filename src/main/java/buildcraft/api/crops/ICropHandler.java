/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.crops;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface ICropHandler {
   boolean isSeed(ItemStack var1);

   boolean canSustainPlant(Level var1, ItemStack var2, BlockPos var3);

   boolean plantCrop(Level var1, Player var2, ItemStack var3, BlockPos var4);

   boolean isMature(BlockGetter var1, BlockState var2, BlockPos var3);

   boolean harvestCrop(Level var1, BlockPos var2, NonNullList<ItemStack> var3);
}

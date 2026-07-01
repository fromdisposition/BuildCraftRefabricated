/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.common;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface IShearable {
   default boolean isShearable(Player player, ItemStack item, Level level, BlockPos pos) {
      return this.isShearable(level, pos);
   }

   default boolean isShearable(Level level, BlockPos pos) {
      return true;
   }

   default List<ItemStack> onSheared(Player player, ItemStack item, Level level, BlockPos pos) {
      return List.of();
   }
}

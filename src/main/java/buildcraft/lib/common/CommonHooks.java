/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.common;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public final class CommonHooks {
   private CommonHooks() {
   }

   public static boolean canPlayerDestroy(Player player, BlockPos pos, BlockState state) {
      return !player.isSpectator() && player.mayBuild();
   }

   public static boolean onPlayerTossEvent(Player player, ItemStack stack, boolean dropAround, boolean includeThrowerName) {
      return true;
   }
}

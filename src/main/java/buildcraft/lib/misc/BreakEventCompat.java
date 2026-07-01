/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents.Before;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class BreakEventCompat {
   private BreakEventCompat() {
   }

   public static boolean canBreak(ServerLevel level, BlockPos pos, BlockState state, Player player) {
      BlockEntity blockEntity = level.getBlockEntity(pos);
      return ((Before)PlayerBlockBreakEvents.BEFORE.invoker()).beforeBlockBreak(level, player, pos, state, blockEntity);
   }
}

/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

// Stand-in for 26.1's net.minecraft.client.color.block.BlockTintSource on 1.21.x: the stonecutter import
// redirect swaps the vanilla import for this one so gated-out 26.1 code still compiles there.
@FunctionalInterface
public interface BlockTintSource {
   int color(BlockState state);

   default int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
      return color(state);
   }
}

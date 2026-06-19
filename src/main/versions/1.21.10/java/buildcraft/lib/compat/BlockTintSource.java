/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 1.21.x stand-in for 26.1's {@code net.minecraft.client.color.block.BlockTintSource}.
 *
 * <p>26.1 models block colours as a list of per-tint-index {@code BlockTintSource}s registered on a
 * block; 1.21.x instead registers a single {@link net.minecraft.client.color.block.BlockColor} that
 * switches on the tint index. BuildCraft keeps writing tint logic against this small interface, and
 * {@code RegisterColorHandlersEvent} adapts a {@code List<BlockTintSource>} into a {@code BlockColor}
 * for the 1.21.x registry.
 */
@FunctionalInterface
public interface BlockTintSource {
   /** Colour with no world context (item / inventory rendering). */
   int color(BlockState state);

   /** Colour with world context (in-level rendering); defaults to the context-free colour. */
   default int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
      return color(state);
   }
}

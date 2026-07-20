/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 1.21.x stand-in for 26.1's {@code net.minecraft.client.color.block.BlockTintSource}.
 *
 * <p>26.1 models block colours as a list of per-tint-index {@code BlockTintSource}s registered on a
 * block; 1.21.x has no such type. This stand-in exists so shared code that imports the 26.1 class in a
 * gated-out branch (e.g. {@code KeyPlugFacade}) still compiles on 1.21.x — the stonecutter import
 * redirect in {@code stonecutter.gradle.kts} swaps the vanilla import for this one.
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

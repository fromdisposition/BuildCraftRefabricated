/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.schematics;

import javax.annotation.Nonnull;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class SchematicBlockContext {
   @Nonnull
   public final Level world;
   @Nonnull
   public final BlockPos basePos;
   @Nonnull
   public final BlockPos pos;
   @Nonnull
   public final BlockState blockState;
   @Nonnull
   public final Block block;

   public SchematicBlockContext(@Nonnull Level world, @Nonnull BlockPos basePos, @Nonnull BlockPos pos, @Nonnull BlockState blockState, @Nonnull Block block) {
      this.world = world;
      this.basePos = basePos;
      this.pos = pos;
      this.blockState = blockState;
      this.block = block;
   }
}

/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport;

import buildcraft.fabric.BCRegistries;
import buildcraft.transport.block.BlockFilteredBuffer;
import buildcraft.transport.block.BlockPipeHolder;
import net.minecraft.world.level.block.SoundType;

public final class BCTransportBlocks {
   public static BlockFilteredBuffer FILTERED_BUFFER;
   public static BlockPipeHolder PIPE_HOLDER;

   private BCTransportBlocks() {
   }

   public static void register() {
      FILTERED_BUFFER = BCRegistries.registerBlock(
         "buildcrafttransport", "filtered_buffer", BlockFilteredBuffer::new, p -> p.strength(5.0F, 10.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()
      );
      PIPE_HOLDER = BCRegistries.registerBlock(
         "buildcrafttransport", "pipe_holder", BlockPipeHolder::new, p -> p.strength(0.25F, 3.0F).noOcclusion().dynamicShape().sound(SoundType.METAL)
      );
   }
}

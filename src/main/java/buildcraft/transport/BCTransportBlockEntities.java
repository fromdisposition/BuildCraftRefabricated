/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport;

import buildcraft.fabric.BCRegistries;
import buildcraft.transport.tile.TileFilteredBuffer;
import buildcraft.transport.tile.TilePipeHolder;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class BCTransportBlockEntities {
   public static BlockEntityType<TileFilteredBuffer> FILTERED_BUFFER;
   public static BlockEntityType<TilePipeHolder> PIPE_HOLDER;

   private BCTransportBlockEntities() {
   }

   public static void register() {
      FILTERED_BUFFER = BCRegistries.registerBlockEntity("buildcrafttransport", "filtered_buffer", TileFilteredBuffer::new, BCTransportBlocks.FILTERED_BUFFER);
      PIPE_HOLDER = BCRegistries.registerBlockEntity("buildcrafttransport", "pipe_holder", TilePipeHolder::new, BCTransportBlocks.PIPE_HOLDER);
   }
}

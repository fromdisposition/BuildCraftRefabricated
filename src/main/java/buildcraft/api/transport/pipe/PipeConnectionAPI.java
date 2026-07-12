/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.transport.pipe;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.world.level.block.Block;

public final class PipeConnectionAPI {
   private static final Map<Block, ICustomPipeConnection> connections = Maps.newHashMap();
   private static final ICustomPipeConnection NOTHING = (world, pos, face, state) -> 0.0F;

   public static void registerConnection(Block block, ICustomPipeConnection connection) {
      connections.put(block, connection);
   }

   public static void registerConnectionAsNothing(Block block) {
      connections.put(block, NOTHING);
   }

   public static ICustomPipeConnection getCustomConnection(Block block) {
      if (block instanceof ICustomPipeConnection) {
         return (ICustomPipeConnection)block;
      }

      ICustomPipeConnection connection = connections.get(block);
      return connection != null ? connection : null;
   }
}

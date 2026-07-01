/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics;

import buildcraft.fabric.BCRegistries;
import buildcraft.robotics.tile.TileRequester;
import buildcraft.robotics.tile.TileZonePlanner;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class BCRoboticsBlockEntities {
   public static BlockEntityType<TileZonePlanner> ZONE_PLANNER;
   public static BlockEntityType<TileRequester> REQUESTER;

   private BCRoboticsBlockEntities() {
   }

   public static void register() {
      if (BCRoboticsBlocks.ZONE_PLANNER != null) {
         ZONE_PLANNER = BCRegistries.registerBlockEntity("buildcraftrobotics", "zone_planner", TileZonePlanner::new, BCRoboticsBlocks.ZONE_PLANNER);
      }

      if (BCRoboticsBlocks.REQUESTER != null) {
         REQUESTER = BCRegistries.registerBlockEntity("buildcraftrobotics", "requester", TileRequester::new, BCRoboticsBlocks.REQUESTER);
      }
   }
}

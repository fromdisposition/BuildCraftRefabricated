/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics;

import buildcraft.fabric.BCRegistries;
import buildcraft.lib.block.BcHorizontalTileBlock;
import buildcraft.robotics.block.BlockRequester;
import buildcraft.robotics.tile.TileZonePlanner;
import net.minecraft.world.level.block.SoundType;

public final class BCRoboticsBlocks {
   public static BcHorizontalTileBlock ZONE_PLANNER;
   public static BlockRequester REQUESTER;

   private BCRoboticsBlocks() {
   }

   public static void register() {
      ZONE_PLANNER = BCRegistries.registerBlock(
         "buildcraftrobotics",
         "zone_planner",
         p -> new BcHorizontalTileBlock(p, () -> BCRoboticsBlockEntities.ZONE_PLANNER, TileZonePlanner::serverTick, null, true),
         p -> p.strength(5.0F, 10.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()
      );
      REQUESTER = BCRegistries.registerBlock(
         "buildcraftrobotics", "requester", BlockRequester::new, p -> p.strength(5.0F, 10.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()
      );
   }
}

/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core;

import buildcraft.core.tile.TileEngineCreative;
import buildcraft.core.tile.TileEngineRedstone_BC8;
import buildcraft.core.tile.TileMarkerPath;
import buildcraft.core.tile.TileMarkerVolume;
import buildcraft.core.tile.TilePowerConsumerTester;
import buildcraft.fabric.BCRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class BCCoreBlockEntities {
   public static BlockEntityType<TileMarkerVolume> MARKER_VOLUME;
   public static BlockEntityType<TileMarkerPath> MARKER_PATH;
   public static BlockEntityType<TileEngineRedstone_BC8> ENGINE_REDSTONE;
   public static BlockEntityType<TileEngineCreative> ENGINE_CREATIVE;
   public static BlockEntityType<TilePowerConsumerTester> POWER_TESTER;

   private BCCoreBlockEntities() {
   }

   public static void register() {
      if (BCCoreBlocks.MARKER_VOLUME != null) {
         MARKER_VOLUME = BCRegistries.registerBlockEntity("buildcraftcore", "marker_volume", TileMarkerVolume::new, BCCoreBlocks.MARKER_VOLUME);
      }

      if (BCCoreBlocks.MARKER_PATH != null) {
         MARKER_PATH = BCRegistries.registerBlockEntity("buildcraftcore", "marker_path", TileMarkerPath::new, BCCoreBlocks.MARKER_PATH);
      }

      if (BCCoreBlocks.ENGINE_REDSTONE != null) {
         ENGINE_REDSTONE = BCRegistries.registerBlockEntity("buildcraftcore", "engine_redstone", TileEngineRedstone_BC8::new, BCCoreBlocks.ENGINE_REDSTONE);
      }

      if (BCCoreBlocks.ENGINE_CREATIVE != null) {
         ENGINE_CREATIVE = BCRegistries.registerBlockEntity("buildcraftcore", "engine_creative", TileEngineCreative::new, BCCoreBlocks.ENGINE_CREATIVE);
      }

      if (BCCore.DEV && BCCoreBlocks.POWER_TESTER != null) {
         POWER_TESTER = BCRegistries.registerBlockEntity("buildcraftcore", "power_tester", TilePowerConsumerTester::new, BCCoreBlocks.POWER_TESTER);
      }
   }
}

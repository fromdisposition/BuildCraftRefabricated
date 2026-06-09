/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core;

import buildcraft.api.enums.EnumSpring;
import buildcraft.core.block.BlockEngineCreative;
import buildcraft.core.block.BlockEngineRedstone_BC8;
import buildcraft.core.block.BlockMarkerPath;
import buildcraft.core.block.BlockMarkerVolume;
import buildcraft.core.block.BlockPowerConsumerTester;
import buildcraft.core.block.BlockSpring;
import buildcraft.fabric.BCRegistries;
import buildcraft.lib.BCLib;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;

public class BCCoreBlocks {
   public static BlockSpring SPRING_WATER;
   public static BlockSpring SPRING_OIL;
   public static Block DECORATED_LASER;
   public static Block DECORATED_DESTROY;
   public static Block DECORATED_BLUEPRINT;
   public static Block DECORATED_TEMPLATE;
   public static Block DECORATED_PAPER;
   public static Block DECORATED_LEATHER;
   public static BlockMarkerVolume MARKER_VOLUME;
   public static BlockMarkerPath MARKER_PATH;
   public static BlockEngineRedstone_BC8 ENGINE_REDSTONE;
   public static BlockEngineCreative ENGINE_CREATIVE;
   public static BlockPowerConsumerTester POWER_TESTER;

   private BCCoreBlocks() {
   }

   public static void register() {
      SPRING_WATER = BCRegistries.registerBlock(
         "buildcraftcore", "spring_water", props -> new BlockSpring(EnumSpring.WATER, props), p -> p.sound(SoundType.STONE)
      );
      SPRING_OIL = BCRegistries.registerBlock("buildcraftcore", "spring_oil", props -> new BlockSpring(EnumSpring.OIL, props), p -> p.sound(SoundType.STONE));
      DECORATED_LASER = BCRegistries.registerBlock(
         "buildcraftcore", "decorated_laser", Block::new, p -> p.strength(3.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()
      );
      DECORATED_DESTROY = BCRegistries.registerBlock(
         "buildcraftcore", "decorated_destroy", Block::new, p -> p.strength(3.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()
      );
      DECORATED_BLUEPRINT = BCRegistries.registerBlock(
         "buildcraftcore",
         "decorated_blueprint",
         Block::new,
         p -> p.strength(3.0F).lightLevel(s -> 10).sound(SoundType.METAL).requiresCorrectToolForDrops()
      );
      DECORATED_TEMPLATE = BCRegistries.registerBlock(
         "buildcraftcore",
         "decorated_template",
         Block::new,
         p -> p.strength(3.0F).lightLevel(s -> 10).sound(SoundType.METAL).requiresCorrectToolForDrops()
      );
      MARKER_VOLUME = BCRegistries.registerBlock("buildcraftcore", "marker_volume", BlockMarkerVolume::new, p -> p.sound(SoundType.METAL));
      MARKER_PATH = BCRegistries.registerBlock("buildcraftcore", "marker_path", BlockMarkerPath::new, p -> p.sound(SoundType.METAL));
      ENGINE_REDSTONE = BCRegistries.registerBlock(
         "buildcraftcore", "engine_redstone", BlockEngineRedstone_BC8::new, p -> p.strength(3.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()
      );
      ENGINE_CREATIVE = BCRegistries.registerBlock(
         "buildcraftcore", "engine_creative", BlockEngineCreative::new, p -> p.strength(3.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()
      );
      if (BCLib.DEV) {
         DECORATED_PAPER = BCRegistries.registerBlock(
            "buildcraftcore",
            "decorated_paper",
            Block::new,
            p -> p.strength(3.0F).lightLevel(s -> 10).sound(SoundType.METAL).requiresCorrectToolForDrops()
         );
         DECORATED_LEATHER = BCRegistries.registerBlock(
            "buildcraftcore",
            "decorated_leather",
            Block::new,
            p -> p.strength(3.0F).lightLevel(s -> 10).sound(SoundType.METAL).requiresCorrectToolForDrops()
         );
         POWER_TESTER = BCRegistries.registerBlock(
            "buildcraftcore", "power_tester", BlockPowerConsumerTester::new, p -> p.strength(3.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()
         );
      }
   }
}

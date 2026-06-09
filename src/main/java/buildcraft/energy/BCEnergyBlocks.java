/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy;

import buildcraft.energy.blocks.BlockDynamoMJ;
import buildcraft.energy.blocks.BlockEngineFE;
import buildcraft.energy.blocks.BlockEngineIron_BC8;
import buildcraft.energy.blocks.BlockEngineStone_BC8;
import buildcraft.fabric.BCRegistries;
import net.minecraft.world.level.block.SoundType;

public final class BCEnergyBlocks {
   public static BlockEngineStone_BC8 ENGINE_STONE;
   public static BlockEngineIron_BC8 ENGINE_IRON;
   public static BlockEngineFE ENGINE_FE;
   public static BlockDynamoMJ DYNAMO_MJ;

   private BCEnergyBlocks() {
   }

   public static void register() {
      ENGINE_STONE = BCRegistries.registerBlock(
         "buildcraftenergy", "engine_stone", BlockEngineStone_BC8::new, p -> p.strength(3.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()
      );
      ENGINE_IRON = BCRegistries.registerBlock(
         "buildcraftenergy", "engine_iron", BlockEngineIron_BC8::new, p -> p.strength(3.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()
      );
      if (BCEnergyConfig.enableRfEngine.get()) {
         ENGINE_FE = BCRegistries.registerBlock(
            "buildcraftenergy", "engine_rf", BlockEngineFE::new, p -> p.strength(3.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()
         );
      }

      if (BCEnergyConfig.enableMjDynamo.get()) {
         DYNAMO_MJ = BCRegistries.registerBlock(
            "buildcraftenergy", "mj_dynamo", BlockDynamoMJ::new, p -> p.strength(3.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()
         );
      }
   }
}

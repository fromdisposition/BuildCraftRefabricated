/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory;

import buildcraft.fabric.BCRegistries;
import buildcraft.factory.block.BlockAutoWorkbenchItems;
import buildcraft.factory.block.BlockChute;
import buildcraft.factory.block.BlockDistiller;
import buildcraft.factory.block.BlockFloodGate;
import buildcraft.factory.block.BlockHeatExchange;
import buildcraft.factory.block.BlockMiningWell;
import buildcraft.factory.block.BlockPump;
import buildcraft.factory.block.BlockTank;
import buildcraft.factory.block.BlockWaterGel;
import net.minecraft.world.level.block.SoundType;

public final class BCFactoryBlocks {
   public static BlockAutoWorkbenchItems AUTOWORKBENCH_ITEM;
   public static BlockMiningWell MINING_WELL;
   public static BlockPump PUMP;
   public static BlockFloodGate FLOOD_GATE;
   public static BlockTank TANK;
   public static BlockChute CHUTE;
   public static BlockDistiller DISTILLER;
   public static BlockHeatExchange HEAT_EXCHANGE;
   public static BlockWaterGel WATER_GEL;

   private BCFactoryBlocks() {
   }

   public static void register() {
      AUTOWORKBENCH_ITEM = BCRegistries.registerBlock(
         "buildcraftfactory", "autoworkbench_item", BlockAutoWorkbenchItems::new, p -> p.strength(3.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()
      );
      MINING_WELL = BCRegistries.registerBlock(
         "buildcraftfactory", "mining_well", BlockMiningWell::new, p -> p.strength(5.0F, 10.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()
      );
      PUMP = BCRegistries.registerBlock(
         "buildcraftfactory", "pump", BlockPump::new, p -> p.strength(5.0F, 10.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()
      );
      FLOOD_GATE = BCRegistries.registerBlock(
         "buildcraftfactory", "flood_gate", BlockFloodGate::new, p -> p.strength(5.0F, 10.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()
      );
      TANK = BCRegistries.registerBlock(
         "buildcraftfactory", "tank", BlockTank::new, p -> p.strength(0.3F).noOcclusion().sound(SoundType.GLASS).requiresCorrectToolForDrops()
      );
      CHUTE = BCRegistries.registerBlock(
         "buildcraftfactory", "chute", BlockChute::new, p -> p.strength(5.0F, 10.0F).noOcclusion().sound(SoundType.METAL).requiresCorrectToolForDrops()
      );
      DISTILLER = BCRegistries.registerBlock(
         "buildcraftfactory", "distiller", BlockDistiller::new, p -> p.strength(5.0F, 10.0F).noOcclusion().sound(SoundType.METAL).requiresCorrectToolForDrops()
      );
      HEAT_EXCHANGE = BCRegistries.registerBlock(
         "buildcraftfactory",
         "heat_exchange",
         BlockHeatExchange::new,
         p -> p.strength(5.0F, 10.0F).noOcclusion().sound(SoundType.METAL).requiresCorrectToolForDrops()
      );
      WATER_GEL = BCRegistries.registerBlock("buildcraftfactory", "water_gel", BlockWaterGel::new, p -> p.strength(0.6F).sound(SoundType.SLIME_BLOCK));
   }
}

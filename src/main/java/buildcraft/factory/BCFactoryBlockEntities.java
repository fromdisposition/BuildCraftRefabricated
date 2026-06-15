/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory;

import buildcraft.fabric.BCRegistries;
import buildcraft.factory.tile.TileAutoWorkbenchFluids;
import buildcraft.factory.tile.TileAutoWorkbenchItems;
import buildcraft.factory.tile.TileChute;
import buildcraft.factory.tile.TileDistiller;
import buildcraft.factory.tile.TileFloodGate;
import buildcraft.factory.tile.TileHeatExchange;
import buildcraft.factory.tile.TileMiningWell;
import buildcraft.factory.tile.TilePump;
import buildcraft.factory.tile.TileTank;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class BCFactoryBlockEntities {
   public static BlockEntityType<TileAutoWorkbenchItems> AUTO_WORKBENCH_ITEMS;
   public static BlockEntityType<TileAutoWorkbenchFluids> AUTO_WORKBENCH_FLUIDS;
   public static BlockEntityType<TileMiningWell> MINING_WELL;
   public static BlockEntityType<TilePump> PUMP;
   public static BlockEntityType<TileFloodGate> FLOOD_GATE;
   public static BlockEntityType<TileTank> TANK;
   public static BlockEntityType<TileChute> CHUTE;
   public static BlockEntityType<TileDistiller> DISTILLER;
   public static BlockEntityType<TileHeatExchange> HEAT_EXCHANGE;

   private BCFactoryBlockEntities() {
   }

   public static void register() {
      AUTO_WORKBENCH_ITEMS = BCRegistries.registerBlockEntity(
         "buildcraftfactory", "autoworkbench_item", TileAutoWorkbenchItems::new, BCFactoryBlocks.AUTOWORKBENCH_ITEM
      );
      AUTO_WORKBENCH_FLUIDS = BCRegistries.registerBlockEntity(
         "buildcraftfactory", "autoworkbench_fluid", TileAutoWorkbenchFluids::new, BCFactoryBlocks.AUTOWORKBENCH_FLUID
      );
      MINING_WELL = BCRegistries.registerBlockEntity("buildcraftfactory", "mining_well", TileMiningWell::new, BCFactoryBlocks.MINING_WELL);
      PUMP = BCRegistries.registerBlockEntity("buildcraftfactory", "pump", TilePump::new, BCFactoryBlocks.PUMP);
      FLOOD_GATE = BCRegistries.registerBlockEntity("buildcraftfactory", "flood_gate", TileFloodGate::new, BCFactoryBlocks.FLOOD_GATE);
      TANK = BCRegistries.registerBlockEntity("buildcraftfactory", "tank", TileTank::new, BCFactoryBlocks.TANK);
      CHUTE = BCRegistries.registerBlockEntity("buildcraftfactory", "chute", TileChute::new, BCFactoryBlocks.CHUTE);
      DISTILLER = BCRegistries.registerBlockEntity("buildcraftfactory", "distiller", TileDistiller::new, BCFactoryBlocks.DISTILLER);
      HEAT_EXCHANGE = BCRegistries.registerBlockEntity("buildcraftfactory", "heat_exchange", TileHeatExchange::new, BCFactoryBlocks.HEAT_EXCHANGE);
   }
}

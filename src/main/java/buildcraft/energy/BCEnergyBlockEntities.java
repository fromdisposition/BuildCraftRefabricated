/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy;

import buildcraft.core.BCCoreBlocks;
import buildcraft.core.block.BlockSpring;
import buildcraft.energy.tile.TileDynamoMJ;
import buildcraft.energy.tile.TileEngineIron_BC8;
import buildcraft.energy.tile.TileEngineRF;
import buildcraft.energy.tile.TileEngineStone_BC8;
import buildcraft.energy.tile.TileSpringOil;
import buildcraft.fabric.BCRegistries;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public final class BCEnergyBlockEntities {
   public static BlockEntityType<TileSpringOil> SPRING_OIL;
   public static BlockEntityType<TileEngineStone_BC8> ENGINE_STONE;
   public static BlockEntityType<TileEngineIron_BC8> ENGINE_IRON;
   public static BlockEntityType<TileEngineRF> ENGINE_FE;
   public static BlockEntityType<TileDynamoMJ> DYNAMO_MJ;

   private BCEnergyBlockEntities() {
   }

   private static TileEngineRF createEngineRF(BlockPos pos, BlockState state) {
      return new TileEngineRF(pos, state);
   }

   private static TileDynamoMJ createDynamoMJ(BlockPos pos, BlockState state) {
      return new TileDynamoMJ(pos, state);
   }

   public static void register() {
      if (BCCoreBlocks.SPRING_OIL != null) {
         SPRING_OIL = BCRegistries.registerBlockEntity("buildcraftenergy", "spring_oil", TileSpringOil::new, BCCoreBlocks.SPRING_OIL);
         BlockSpring.oilTileFactory = TileSpringOil::new;
      }

      if (BCEnergyBlocks.ENGINE_STONE != null) {
         ENGINE_STONE = BCRegistries.registerBlockEntity("buildcraftenergy", "engine_stone", TileEngineStone_BC8::new, BCEnergyBlocks.ENGINE_STONE);
      }

      if (BCEnergyBlocks.ENGINE_IRON != null) {
         ENGINE_IRON = BCRegistries.registerBlockEntity("buildcraftenergy", "engine_iron", TileEngineIron_BC8::new, BCEnergyBlocks.ENGINE_IRON);
      }

      if (FabricLoader.getInstance().isModLoaded("team_reborn_energy") && BCEnergyBlocks.ENGINE_FE != null) {
         ENGINE_FE = BCRegistries.registerBlockEntity("buildcraftenergy", "engine_rf", BCEnergyBlockEntities::createEngineRF, BCEnergyBlocks.ENGINE_FE);
      }

      if (FabricLoader.getInstance().isModLoaded("team_reborn_energy") && BCEnergyBlocks.DYNAMO_MJ != null) {
         DYNAMO_MJ = BCRegistries.registerBlockEntity("buildcraftenergy", "mj_dynamo", BCEnergyBlockEntities::createDynamoMJ, BCEnergyBlocks.DYNAMO_MJ);
      }
   }
}

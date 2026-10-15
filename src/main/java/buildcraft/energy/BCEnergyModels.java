/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy;

import buildcraft.energy.tile.TileDynamoMJ;
import buildcraft.energy.tile.TileEngineIron_BC8;
import buildcraft.energy.tile.TileEngineRF;
import buildcraft.energy.tile.TileEngineStone_BC8;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.render.tile.EngineModel;
import buildcraft.lib.client.render.tile.EngineModelCache;

public class BCEnergyModels {
   private static final EngineModel STONE = EngineModel.engine("buildcraftenergy:block/engine/stone_back", "buildcraftenergy:block/engine/stone_side");
   private static final EngineModel IRON = EngineModel.engine("buildcraftenergy:block/engine/iron_back", "buildcraftenergy:block/engine/iron_side");
   private static final EngineModel FE = EngineModel.engine("buildcraftenergy:block/engine/fe_back", "buildcraftenergy:block/engine/fe_side");
   private static final EngineModel DYNAMO = EngineModel.dynamo(
      "buildcraftenergy:block/mj_dynamo/back", "buildcraftenergy:block/mj_dynamo/side", "buildcraftenergy:block/mj_dynamo/front"
   );
   private static final EngineModelCache ENGINE_STONE = new EngineModelCache(STONE);
   private static final EngineModelCache ENGINE_IRON = new EngineModelCache(IRON);
   private static final EngineModelCache ENGINE_FE = new EngineModelCache(FE);
   private static final EngineModelCache DYNAMO_MJ = new EngineModelCache(DYNAMO);

   public static MutableQuad[] getStoneEngineQuads(TileEngineStone_BC8 tile, float partialTicks) {
      return ENGINE_STONE.getQuads(tile, partialTicks);
   }

   public static MutableQuad[] getIronEngineQuads(TileEngineIron_BC8 tile, float partialTicks) {
      return ENGINE_IRON.getQuads(tile, partialTicks);
   }

   public static MutableQuad[] getFeEngineQuads(TileEngineRF tile, float partialTicks) {
      return ENGINE_FE.getQuads(tile, partialTicks);
   }

   public static MutableQuad[] getDynamoQuads(TileDynamoMJ tile, float partialTicks) {
      return DYNAMO_MJ.getQuads(tile, partialTicks);
   }
}

/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core;

import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.render.tile.EngineModel;
import buildcraft.lib.client.render.tile.EngineModelCache;
import buildcraft.lib.engine.TileEngineBase_BC8;

public class BCCoreModels {
   private static final EngineModel WOOD = EngineModel.engine("buildcraftcore:block/engine/wood_back", "buildcraftcore:block/engine/wood_side");
   private static final EngineModel CREATIVE = EngineModel.engine("buildcraftcore:block/engine/creative_back", "buildcraftcore:block/engine/creative_side");
   private static final EngineModelCache ENGINE_WOOD = new EngineModelCache(WOOD);
   private static final EngineModelCache ENGINE_CREATIVE = new EngineModelCache(CREATIVE);

   public static MutableQuad[] getWoodEngineQuads(TileEngineBase_BC8 tile, float partialTicks) {
      return ENGINE_WOOD.getQuads(tile, partialTicks);
   }

   public static MutableQuad[] getCreativeEngineQuads(TileEngineBase_BC8 tile, float partialTicks) {
      return ENGINE_CREATIVE.getQuads(tile, partialTicks);
   }
}

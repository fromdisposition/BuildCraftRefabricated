/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.render.tile;

import buildcraft.api.enums.EnumPowerStage;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.engine.TileEngineBase_BC8;
import java.util.Arrays;
import net.minecraft.core.Direction;

public final class EngineModelCache {
   private static final int PROGRESS_QUANTIZATION = 128;
   private static final int FACING_COUNT = 6;
   private static final int PROGRESS_VALUES = 129;
   private static final int CACHE_SIZE = EnumPowerStage.values().length * 129 * 6;
   private static int currentBakeId;
   private final EngineModel model;
   private final MutableQuad[][] entries = new MutableQuad[CACHE_SIZE][];
   private int bakeId = -1;

   public EngineModelCache(EngineModel model) {
      this.model = model;
   }

   public static void onModelBake() {
      currentBakeId++;
   }

   private static int cacheKey(EnumPowerStage stage, int progressQuant, Direction facing) {
      return stage.ordinal() * PROGRESS_VALUES * FACING_COUNT + progressQuant * FACING_COUNT + facing.ordinal();
   }

   public MutableQuad[] getQuads(TileEngineBase_BC8 tile, float partialTicks) {
      if (this.bakeId != currentBakeId) {
         Arrays.fill(this.entries, null);
         this.bakeId = currentBakeId;
      }

      float progress = tile.getProgressClient(partialTicks);
      EnumPowerStage stage = tile.getPowerStage();
      Direction facing = tile.getOrientation();
      int progressQuant = Math.max(0, Math.min(PROGRESS_QUANTIZATION, (int)(progress * PROGRESS_QUANTIZATION + 0.5F)));
      int key = cacheKey(stage, progressQuant, facing);
      MutableQuad[] cached = this.entries[key];
      if (cached != null) {
         return cached;
      }

      MutableQuad[] quads = this.model.bake(stage, progressQuant / (double) PROGRESS_QUANTIZATION, facing);
      this.entries[key] = quads;
      return quads;
   }
}

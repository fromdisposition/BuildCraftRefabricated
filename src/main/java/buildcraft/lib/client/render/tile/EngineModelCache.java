/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.render.tile;

import buildcraft.api.enums.EnumPowerStage;
import buildcraft.lib.client.model.ModelHolderVariable;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.model.json.JsonVariableModel;
import buildcraft.lib.engine.TileEngineBase_BC8;
import buildcraft.lib.expression.node.value.NodeVariableDouble;
import buildcraft.lib.expression.node.value.NodeVariableObject;
import java.util.Arrays;
import net.minecraft.core.Direction;

public final class EngineModelCache {
   private static final int PROGRESS_QUANTIZATION = 128;
   private static final int FACING_COUNT = 6;
   private static final int PROGRESS_VALUES = 129;
   private static final int CACHE_SIZE = EnumPowerStage.values().length * 129 * 6;
   private final ModelHolderVariable model;
   private final NodeVariableDouble progressVar;
   private final NodeVariableObject<EnumPowerStage> stageVar;
   private final NodeVariableObject<Direction> facingVar;
   private final MutableQuad[][] entries = new MutableQuad[CACHE_SIZE][];
   private JsonVariableModel lastRawModel;

   public EngineModelCache(
      ModelHolderVariable model, NodeVariableDouble progressVar, NodeVariableObject<EnumPowerStage> stageVar, NodeVariableObject<Direction> facingVar
   ) {
      this.model = model;
      this.progressVar = progressVar;
      this.stageVar = stageVar;
      this.facingVar = facingVar;
   }

   private static int cacheKey(EnumPowerStage stage, int progressQuant, Direction facing) {
      return stage.ordinal() * 129 * 6 + progressQuant * 6 + facing.ordinal();
   }

   public MutableQuad[] getQuads(TileEngineBase_BC8 tile, float partialTicks) {
      JsonVariableModel rawModel = this.model.getModel();
      if (rawModel == null) {
         return MutableQuad.EMPTY_ARRAY;
      }

      if (rawModel != this.lastRawModel) {
         Arrays.fill(this.entries, null);
         this.lastRawModel = rawModel;
      }

      float progress = tile.getProgressClient(partialTicks);
      EnumPowerStage stage = tile.getPowerStage();
      Direction facing = tile.getOrientation();
      int progressQuant = Math.max(0, Math.min(128, (int)(progress * 128.0F + 0.5F)));
      int key = cacheKey(stage, progressQuant, facing);
      MutableQuad[] cached = this.entries[key];
      if (cached != null) {
         return cached;
      }

      this.progressVar.value = progressQuant / 128.0;
      this.stageVar.value = stage;
      this.facingVar.value = facing;
      if (tile.clientModelData.hasNoNodes()) {
         tile.clientModelData.setNodes(this.model.createTickableNodes());
      }

      tile.clientModelData.refresh();
      MutableQuad[] quads = this.model.getCutoutQuads();
      this.entries[key] = quads;
      return quads;
   }
}

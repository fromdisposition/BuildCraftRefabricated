/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.model;

import buildcraft.transport.client.model.key.PipeModelKey;
import buildcraft.transport.pipe.Pipe;
import buildcraft.transport.tile.TilePipeHolder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;

public class ModelPipe {
   public static final int PIPE_PAINT_ALPHA = 76;

   public static void renderDirect(TilePipeHolder tile, Pose pose, VertexConsumer buffer, int light) {
      if (tile != null && tile.getPipe() != null) {
         Pipe pipe = tile.getPipe();
         PipeModelCacheBase.PipeBaseCutoutKey cutoutKey = pipe.getCutoutKey();
         if (cutoutKey != null) {
            PipeMutableQuadCache.renderCutout(cutoutKey, pose, buffer, light);
         }
      }
   }

   public static void renderCutoutPluggables(TilePipeHolder tile, Pose pose, VertexConsumer buffer, int light) {
      if (tile != null && tile.getPipe() != null) {
         PipeModelCachePluggable.PluggableKey key = new PipeModelCachePluggable.PluggableKey(true, tile);
         PipePluggableQuadCache.renderCutout(key, pose, buffer, light);
      }
   }

   public static void renderMaskOverlay(TilePipeHolder tile, Pose pose, VertexConsumer buffer, int light, int alpha) {
      if (tile != null && tile.getPipe() != null) {
         Pipe pipe = tile.getPipe();
         PipeModelCacheBase.PipeBaseCutoutKey cutoutKey = pipe.getCutoutKey();
         if (cutoutKey != null) {
            PipeMutableQuadCache.renderMask(cutoutKey, pose, buffer, light, alpha);
         }
      }
   }

   public static void renderDirect(PipeModelKey modelKey, Pose pose, VertexConsumer buffer, int light) {
      if (modelKey != null) {
         PipeMutableQuadCache.renderCutout(new PipeModelCacheBase.PipeBaseCutoutKey(modelKey), pose, buffer, light);
      }
   }

   public static void renderMaskOverlay(PipeModelKey modelKey, Pose pose, VertexConsumer buffer, int light, int alpha) {
      if (modelKey != null) {
         PipeMutableQuadCache.renderMask(new PipeModelCacheBase.PipeBaseCutoutKey(modelKey), pose, buffer, light, alpha);
      }
   }
}

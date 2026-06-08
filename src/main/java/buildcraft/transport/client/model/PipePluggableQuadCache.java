/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.model;

import buildcraft.lib.client.model.BakedQuadTemplateCache;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;

public final class PipePluggableQuadCache {
   private static final BakedQuadTemplateCache<PipeModelCachePluggable.PluggableKey> CUTOUT = new BakedQuadTemplateCache<>(
      PipeModelCachePluggable.cacheCutoutAll::bake
   );

   private PipePluggableQuadCache() {
   }

   public static void renderCutout(PipeModelCachePluggable.PluggableKey key, Pose pose, VertexConsumer buffer, int light) {
      CUTOUT.render(key, pose, buffer, light);
   }
}

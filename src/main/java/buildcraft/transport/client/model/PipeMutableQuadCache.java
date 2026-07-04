/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.model;

import buildcraft.lib.client.model.BakedQuadTemplateCache;
import buildcraft.lib.client.model.MutableQuad;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PipeMutableQuadCache {
   private static final BakedQuadTemplateCache<PipeModelCacheBase.PipeBaseCutoutKey> CUTOUT = new BakedQuadTemplateCache<>(PipeModelCacheBase.cacheCutout::bake);
   private static final Map<PipeMutableQuadCache.MaskCacheKey, List<MutableQuad>> MASK = new ConcurrentHashMap<>();

   private PipeMutableQuadCache() {
   }

   public static void clearCaches() {
      CUTOUT.clear();
      MASK.clear();
   }

   public static MutableQuad renderScratch() {
      return BakedQuadTemplateCache.renderScratch();
   }

   public static void renderCutout(PipeModelCacheBase.PipeBaseCutoutKey key, Pose pose, VertexConsumer buffer, int light) {
      CUTOUT.render(key, pose, buffer, light);
   }

   /**
    * Cached paint-mask templates for (pipe shape, alpha) — dye and alpha are baked into the vertex colours.
    * Safe to call from chunk-build worker threads: the map is concurrent and the generator only reads
    * immutable templates and atlas sprites.
    */
   public static List<MutableQuad> maskQuads(PipeModelCacheBase.PipeBaseCutoutKey key, int alpha) {
      return MASK.computeIfAbsent(
         new PipeMutableQuadCache.MaskCacheKey(key, alpha), k -> PipeBaseModelGenStandard.INSTANCE.generateMaskMutable(k.key(), k.alpha())
      );
   }

   public static void renderMask(PipeModelCacheBase.PipeBaseCutoutKey key, Pose pose, VertexConsumer buffer, int light, int alpha) {
      MutableQuad scratch = BakedQuadTemplateCache.renderScratch();

      for (MutableQuad template : maskQuads(key, alpha)) {
         scratch.copyFrom(template);
         scratch.lighti(light);
         scratch.render(pose, buffer);
      }
   }

   private record MaskCacheKey(PipeModelCacheBase.PipeBaseCutoutKey key, int alpha) {
   }
}

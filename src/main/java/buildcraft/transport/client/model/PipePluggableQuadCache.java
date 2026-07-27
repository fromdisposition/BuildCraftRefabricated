/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.model;

import buildcraft.api.transport.pluggable.PluggableModelKey;
import buildcraft.lib.client.model.BakedQuadTemplateCache;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;

public final class PipePluggableQuadCache {
   private static final BakedQuadTemplateCache<PipeModelCachePluggable.PluggableKey> CUTOUT = new BakedQuadTemplateCache<>(
      PipeModelCachePluggable.cacheCutoutAll::bake
   );
   private static final BakedQuadTemplateCache<PluggableModelKey> TRANSLUCENT = new BakedQuadTemplateCache<>(
      PipeModelCachePluggable.cacheTranslucentSingle::bake, false
   );
   private static final BakedQuadTemplateCache<PluggableModelKey> CUTOUT_SINGLE = new BakedQuadTemplateCache<>(
      PipeModelCachePluggable.cacheCutoutSingle::bake
   );

   private PipePluggableQuadCache() {
   }

   /**
    * Drops the render templates AND the baked-quad caches underneath them on a resource/model reload — without
    * this, every pluggable kept rendering with the pre-reload atlas sprites until a full restart.
    */
   public static void clearCaches() {
      CUTOUT.clear();
      TRANSLUCENT.clear();
      CUTOUT_SINGLE.clear();
      PipeModelCachePluggable.cacheCutoutSingle.clear();
      PipeModelCachePluggable.cacheTranslucentSingle.clear();
      PipeModelCachePluggable.cacheCutoutAll.clear();
   }

   public static void renderCutout(PipeModelCachePluggable.PluggableKey key, Pose pose, VertexConsumer buffer, int light) {
      CUTOUT.render(key, pose, buffer, light);
   }

   /** Per-pluggable cutout render with a per-quad tint resolver -- see PluggableModelKey.resolveWorldTint. */
   public static void renderCutoutTintResolved(
      PluggableModelKey key, Pose pose, VertexConsumer buffer, int light, java.util.function.IntUnaryOperator tintToRgb
   ) {
      CUTOUT_SINGLE.renderTintResolved(key, pose, buffer, light, tintToRgb);
   }

   /** Rendered per single pluggable (not the merged per-tile set) so each keeps its own tint colour — e.g. the
    * lens dye, which must be applied here because the BakedQuad bake drops per-vertex colour on modern MC. */
   public static void renderTranslucent(PluggableModelKey key, Pose pose, VertexConsumer buffer, int light) {
      int tint = key.getTintColour();
      if (tint != -1) {
         TRANSLUCENT.renderTinted(key, pose, buffer, light, tint);
      } else {
         TRANSLUCENT.render(key, pose, buffer, light);
      }
   }
}

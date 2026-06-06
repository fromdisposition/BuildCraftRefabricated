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

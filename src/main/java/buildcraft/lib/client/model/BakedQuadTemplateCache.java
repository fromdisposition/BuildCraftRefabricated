package buildcraft.lib.client.model;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import net.minecraft.client.resources.model.geometry.BakedQuad;

public final class BakedQuadTemplateCache<K> {
   private static final ThreadLocal<MutableQuad> RENDER_SCRATCH = ThreadLocal.withInitial(MutableQuad::new);
   private final Map<K, List<MutableQuad>> templates = new ConcurrentHashMap<>();
   private final Function<K, List<BakedQuad>> baker;

   public BakedQuadTemplateCache(Function<K, List<BakedQuad>> baker) {
      this.baker = baker;
   }

   public static MutableQuad renderScratch() {
      return RENDER_SCRATCH.get();
   }

   public void clear() {
      this.templates.clear();
   }

   private List<MutableQuad> buildTemplates(K key) {
      List<BakedQuad> baked = this.baker.apply(key);
      List<MutableQuad> result = new ArrayList<>(baked.size());

      for (BakedQuad quad : baked) {
         MutableQuad mutable = new MutableQuad().fromBakedBlock(quad);
         mutable.setCalculatedDiffuse();
         result.add(mutable);
      }

      return result;
   }

   public void render(K key, Pose pose, VertexConsumer buffer, int light) {
      MutableQuad scratch = RENDER_SCRATCH.get();

      for (MutableQuad template : this.templates.computeIfAbsent(key, this::buildTemplates)) {
         scratch.copyFrom(template);
         scratch.lighti(light);
         scratch.render(pose, buffer);
      }
   }
}

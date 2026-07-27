/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

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
   private final boolean applyDiffuse;

   public BakedQuadTemplateCache(Function<K, List<BakedQuad>> baker) {
      this(baker, true);
   }

   public BakedQuadTemplateCache(Function<K, List<BakedQuad>> baker, boolean applyDiffuse) {
      this.baker = baker;
      this.applyDiffuse = applyDiffuse;
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
         // setCalculatedDiffuse OVERWRITES the vertex colour with a grey face-shade — correct for sprite-coloured
         // cutout geometry, but it would wipe the per-vertex dye baked into translucent quads (e.g. the lens
         // glass), so translucent caches skip it and keep the baked colour.
         if (this.applyDiffuse) {
            mutable.setCalculatedDiffuse();
         }

         result.add(mutable);
      }

      return result;
   }

   /**
    * As {@link #render}, but resolves a packed RGB per quad from its baked tint index (resolver returns -1 for
    * untinted). The colour MULTIPLIES the template's vertex colour so the diffuse face shade baked into cutout
    * templates survives -- used for world-tinted facades (grass/leaves biome colours).
    */
   public void renderTintResolved(K key, Pose pose, VertexConsumer buffer, int light, java.util.function.IntUnaryOperator tintToRgb) {
      MutableQuad scratch = RENDER_SCRATCH.get();

      for (MutableQuad template : this.templates.computeIfAbsent(key, this::buildTemplates)) {
         scratch.copyFrom(template);
         int tint = template.getTint();
         if (tint >= 0) {
            int rgb = tintToRgb.applyAsInt(tint);
            if (rgb != -1) {
               multiplyColour(scratch, rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF);
            }
         }

         scratch.lighti(light);
         scratch.render(pose, buffer);
      }
   }

   private static void multiplyColour(MutableQuad quad, int r, int g, int b) {
      for (MutableVertex vertex : new MutableVertex[]{quad.vertex_0, quad.vertex_1, quad.vertex_2, quad.vertex_3}) {
         vertex.colouri(
            (vertex.colour_r & 0xFF) * r / 255, (vertex.colour_g & 0xFF) * g / 255, (vertex.colour_b & 0xFF) * b / 255, vertex.colour_a & 0xFF
         );
      }
   }

   public void render(K key, Pose pose, VertexConsumer buffer, int light) {
      MutableQuad scratch = RENDER_SCRATCH.get();

      for (MutableQuad template : this.templates.computeIfAbsent(key, this::buildTemplates)) {
         scratch.copyFrom(template);
         scratch.lighti(light);
         scratch.render(pose, buffer);
      }
   }

   /** As {@link #render}, but tints every vertex with the given packed ARGB — used to colour geometry whose dye
    * cannot survive the {@code BakedQuad} bake (no per-vertex colour on 26.x), e.g. the lens glass. */
   public void renderTinted(K key, Pose pose, VertexConsumer buffer, int light, int argb) {
      int a = argb >>> 24 & 0xFF;
      int r = argb >> 16 & 0xFF;
      int g = argb >> 8 & 0xFF;
      int b = argb & 0xFF;
      MutableQuad scratch = RENDER_SCRATCH.get();

      for (MutableQuad template : this.templates.computeIfAbsent(key, this::buildTemplates)) {
         scratch.copyFrom(template);
         scratch.colouri(r, g, b, a);
         scratch.lighti(light);
         scratch.render(pose, buffer);
      }
   }
}

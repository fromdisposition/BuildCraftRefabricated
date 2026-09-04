/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.model;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.client.resources.model.geometry.BakedQuad;

public final class BakedQuadTemplateCache<K> {
   private static final ThreadLocal<MutableQuad> RENDER_SCRATCH = ThreadLocal.withInitial(MutableQuad::new);
   // Keys span facade state x side x colour x connections; without expiry this grows without bound.
   private final LoadingCache<K, List<MutableQuad>> templates = CacheBuilder.newBuilder()
      //? if >= 26.2 {
      .expireAfterAccess(java.time.Duration.ofMinutes(1))
      //?} else {
      /*.expireAfterAccess(1L, java.util.concurrent.TimeUnit.MINUTES)
      *///?}
      .build(CacheLoader.from(this::buildTemplates));
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
      this.templates.invalidateAll();
   }

   private List<MutableQuad> buildTemplates(K key) {
      List<BakedQuad> baked = this.baker.apply(key);
      List<MutableQuad> result = new ArrayList<>(baked.size());

      for (BakedQuad quad : baked) {
         MutableQuad mutable = new MutableQuad().fromBakedBlock(quad);
         // setCalculatedDiffuse overwrites the vertex colour, which would wipe the dye baked into translucent quads.
         if (this.applyDiffuse) {
            mutable.setCalculatedDiffuse();
         }

         result.add(mutable);
      }

      return result;
   }

   /** Resolver returns -1 for untinted; the colour multiplies the vertex colour so the baked face shade survives. */
   public void renderTintResolved(K key, Pose pose, VertexConsumer buffer, int light, java.util.function.IntUnaryOperator tintToRgb) {
      MutableQuad scratch = RENDER_SCRATCH.get();

      for (MutableQuad template : this.templates.getUnchecked(key)) {
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

      for (MutableQuad template : this.templates.getUnchecked(key)) {
         scratch.copyFrom(template);
         scratch.lighti(light);
         scratch.render(pose, buffer);
      }
   }

   // Per-vertex colour does not survive the BakedQuad bake, so dyed geometry is tinted at render time.
   public void renderTinted(K key, Pose pose, VertexConsumer buffer, int light, int argb) {
      int a = argb >>> 24 & 0xFF;
      int r = argb >> 16 & 0xFF;
      int g = argb >> 8 & 0xFF;
      int b = argb & 0xFF;
      MutableQuad scratch = RENDER_SCRATCH.get();

      for (MutableQuad template : this.templates.getUnchecked(key)) {
         scratch.copyFrom(template);
         scratch.colouri(r, g, b, a);
         scratch.lighti(light);
         scratch.render(pose, buffer);
      }
   }
}

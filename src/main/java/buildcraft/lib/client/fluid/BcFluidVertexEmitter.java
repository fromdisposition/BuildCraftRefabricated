/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.fluid;

import buildcraft.fabric.BCEnergyFluidsFabric;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public final class BcFluidVertexEmitter {
   private BcFluidVertexEmitter() {
   }

   public static void emitQuadWithAtlasUv(
      Pose pose,
      VertexConsumer buffer,
      TextureAtlasSprite sprite,
      BCEnergyFluidsFabric.FluidEntry entry,
      float x1,
      float y1,
      float z1,
      float au1,
      float av1,
      float x2,
      float y2,
      float z2,
      float au2,
      float av2,
      float x3,
      float y3,
      float z3,
      float au3,
      float av3,
      float x4,
      float y4,
      float z4,
      float au4,
      float av4,
      float nx,
      float ny,
      float nz,
      float r,
      float g,
      float b,
      float a,
      int light,
      int overlay
   ) {
      emitQuadWithAtlasUv(
         pose,
         buffer,
         sprite,
         entry,
         entry != null ? 16 : 1,
         x1,
         y1,
         z1,
         au1,
         av1,
         x2,
         y2,
         z2,
         au2,
         av2,
         x3,
         y3,
         z3,
         au3,
         av3,
         x4,
         y4,
         z4,
         au4,
         av4,
         nx,
         ny,
         nz,
         r,
         g,
         b,
         a,
         light,
         overlay
      );
   }

   static void emitQuadWithAtlasUv(
      Pose pose,
      VertexConsumer buffer,
      TextureAtlasSprite sprite,
      BCEnergyFluidsFabric.FluidEntry entry,
      int subdivisions,
      float x1,
      float y1,
      float z1,
      float au1,
      float av1,
      float x2,
      float y2,
      float z2,
      float au2,
      float av2,
      float x3,
      float y3,
      float z3,
      float au3,
      float av3,
      float x4,
      float y4,
      float z4,
      float au4,
      float av4,
      float nx,
      float ny,
      float nz,
      float r,
      float g,
      float b,
      float a,
      int lightOrPacked,
      int overlay
   ) {
      if (entry != null && subdivisions > 1) {
         emitSubdividedQuad(
            pose,
            buffer,
            sprite,
            entry,
            subdivisions,
            x1,
            y1,
            z1,
            x2,
            y2,
            z2,
            x3,
            y3,
            z3,
            x4,
            y4,
            z4,
            au1,
            av1,
            au2,
            av2,
            au3,
            av3,
            au4,
            av4,
            nx,
            ny,
            nz,
            lightOrPacked,
            overlay,
            true
         );
      } else {
         putVertex(pose, buffer, x1, y1, z1, au1, av1, nx, ny, nz, r, g, b, a, lightOrPacked, overlay, false);
         putVertex(pose, buffer, x2, y2, z2, au2, av2, nx, ny, nz, r, g, b, a, lightOrPacked, overlay, false);
         putVertex(pose, buffer, x3, y3, z3, au3, av3, nx, ny, nz, r, g, b, a, lightOrPacked, overlay, false);
         putVertex(pose, buffer, x4, y4, z4, au4, av4, nx, ny, nz, r, g, b, a, lightOrPacked, overlay, false);
      }
   }

   private static void emitSubdividedQuad(
      Pose pose,
      VertexConsumer buffer,
      TextureAtlasSprite sprite,
      BCEnergyFluidsFabric.FluidEntry entry,
      int n,
      float x0,
      float y0,
      float z0,
      float x1,
      float y1,
      float z1,
      float x2,
      float y2,
      float z2,
      float x3,
      float y3,
      float z3,
      float au1,
      float av1,
      float au2,
      float av2,
      float au3,
      float av3,
      float au4,
      float av4,
      float nx,
      float ny,
      float nz,
      int lightOrPacked,
      int overlay,
      boolean packedLight
   ) {
      float[][] xs = new float[n + 1][n + 1];
      float[][] ys = new float[n + 1][n + 1];
      float[][] zs = new float[n + 1][n + 1];
      float[][] us = new float[n + 1][n + 1];
      float[][] vs = new float[n + 1][n + 1];

      for (int i = 0; i <= n; i++) {
         float u = (float)i / n;

         for (int j = 0; j <= n; j++) {
            float v = (float)j / n;
            xs[i][j] = bilinear(x0, x1, x2, x3, u, v);
            ys[i][j] = bilinear(y0, y1, y2, y3, u, v);
            zs[i][j] = bilinear(z0, z1, z2, z3, u, v);
            us[i][j] = bilinear(au1, au2, au3, au4, u, v);
            vs[i][j] = bilinear(av1, av2, av3, av4, u, v);
         }
      }

      for (int i = 0; i < n; i++) {
         for (int j = 0; j < n; j++) {
            emitColoredVertex(pose, buffer, sprite, entry, xs[i][j], ys[i][j], zs[i][j], us[i][j], vs[i][j], nx, ny, nz, lightOrPacked, overlay, packedLight);
            emitColoredVertex(
               pose,
               buffer,
               sprite,
               entry,
               xs[i + 1][j],
               ys[i + 1][j],
               zs[i + 1][j],
               us[i + 1][j],
               vs[i + 1][j],
               nx,
               ny,
               nz,
               lightOrPacked,
               overlay,
               packedLight
            );
            emitColoredVertex(
               pose,
               buffer,
               sprite,
               entry,
               xs[i + 1][j + 1],
               ys[i + 1][j + 1],
               zs[i + 1][j + 1],
               us[i + 1][j + 1],
               vs[i + 1][j + 1],
               nx,
               ny,
               nz,
               lightOrPacked,
               overlay,
               packedLight
            );
            emitColoredVertex(
               pose,
               buffer,
               sprite,
               entry,
               xs[i][j + 1],
               ys[i][j + 1],
               zs[i][j + 1],
               us[i][j + 1],
               vs[i][j + 1],
               nx,
               ny,
               nz,
               lightOrPacked,
               overlay,
               packedLight
            );
         }
      }
   }

   private static void emitColoredVertex(
      Pose pose,
      VertexConsumer buffer,
      TextureAtlasSprite sprite,
      BCEnergyFluidsFabric.FluidEntry entry,
      float x,
      float y,
      float z,
      float u,
      float v,
      float nx,
      float ny,
      float nz,
      int lightOrPacked,
      int overlay,
      boolean packedLight
   ) {
      float nu = BcFluidTintUtil.normalizedU(u, sprite.getU0(), sprite.getU1());
      float nv = BcFluidTintUtil.normalizedV(v, sprite.getV0(), sprite.getV1());
      int argb = BcFluidTintUtil.vertexColorFromTemplate(entry.texLight(), entry.texDark(), entry.heat(), nu, nv);
      float a = (argb >> 24 & 0xFF) / 255.0F;
      float r = (argb >> 16 & 0xFF) / 255.0F;
      float g = (argb >> 8 & 0xFF) / 255.0F;
      float b = (argb & 0xFF) / 255.0F;
      if (a <= 0.0F) {
         a = 1.0F;
      }

      putVertex(pose, buffer, x, y, z, u, v, nx, ny, nz, r, g, b, a, lightOrPacked, overlay, packedLight);
   }

   static void putVertex(
      Pose pose,
      VertexConsumer buffer,
      float x,
      float y,
      float z,
      float u,
      float v,
      float nx,
      float ny,
      float nz,
      float r,
      float g,
      float b,
      float a,
      int lightOrPacked,
      int overlay,
      boolean packedLight
   ) {
      VertexConsumer vertex = buffer.addVertex(pose, x, y, z).setColor(r, g, b, a).setUv(u, v).setOverlay(overlay).setNormal(pose, nx, ny, nz);
      if (packedLight) {
         vertex.setUv2(lightOrPacked & 65535, lightOrPacked >> 16 & 65535);
      } else {
         vertex.setLight(lightOrPacked);
      }
   }

   static float bilinear(float c0, float c1, float c2, float c3, float u, float v) {
      float top = lerp(c0, c1, u);
      float bot = lerp(c3, c2, u);
      return lerp(top, bot, v);
   }

   static float lerp(float a, float b, float t) {
      return a + (b - a) * t;
   }

   static float posU(TextureAtlasSprite sprite, float nx, float x, float z) {
      return sprite.getU(nx != 0.0F ? z : x);
   }

   static float posV(TextureAtlasSprite sprite, float y) {
      return sprite.getV(1.0F - y);
   }
}

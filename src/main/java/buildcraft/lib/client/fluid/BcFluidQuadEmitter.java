/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.fluid;

import buildcraft.fabric.BCEnergyFluidsFabric;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

public final class BcFluidQuadEmitter {
   public static final int SUBDIVISIONS = 16;
   public static final int PIPE_SUBDIVISIONS = 8;

   private BcFluidQuadEmitter() {
   }

   public static void emitTankQuad(
      Pose pose,
      VertexConsumer buffer,
      TextureAtlasSprite sprite,
      float x1,
      float y1,
      float z1,
      float x2,
      float y2,
      float z2,
      float x3,
      float y3,
      float z3,
      float x4,
      float y4,
      float z4,
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
         null,
         1,
         x1,
         y1,
         z1,
         posU(sprite, nx, x1, z1),
         posV(sprite, y1),
         x2,
         y2,
         z2,
         posU(sprite, nx, x2, z2),
         posV(sprite, y2),
         x3,
         y3,
         z3,
         posU(sprite, nx, x3, z3),
         posV(sprite, y3),
         x4,
         y4,
         z4,
         posU(sprite, nx, x4, z4),
         posV(sprite, y4),
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

   public static void emitTankHorizontal(
      Pose pose,
      VertexConsumer buffer,
      TextureAtlasSprite sprite,
      float x1,
      float x2,
      float z1,
      float z2,
      float y,
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
         null,
         1,
         x1,
         y,
         z1,
         sprite.getU(x1),
         sprite.getV(z1),
         x2,
         y,
         z1,
         sprite.getU(x2),
         sprite.getV(z1),
         x2,
         y,
         z2,
         sprite.getU(x2),
         sprite.getV(z2),
         x1,
         y,
         z2,
         sprite.getU(x1),
         sprite.getV(z2),
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

   public static void emitStaticPipeCuboid(
      Pose pose,
      VertexConsumer buffer,
      TextureAtlasSprite sprite,
      float minX,
      float minY,
      float minZ,
      float maxX,
      float maxY,
      float maxZ,
      int skipFaceMask,
      float r,
      float g,
      float b,
      float a,
      int packedLight
   ) {
      int overlay = OverlayTexture.NO_OVERLAY;
      if ((skipFaceMask & 1 << Direction.DOWN.ordinal()) == 0) {
         emitStaticPipeHorizontal(pose, buffer, sprite, Direction.DOWN, minX, maxX, minZ, maxZ, minY, r, g, b, a, packedLight, overlay);
      }

      if ((skipFaceMask & 1 << Direction.UP.ordinal()) == 0) {
         emitStaticPipeHorizontal(pose, buffer, sprite, Direction.UP, minX, maxX, minZ, maxZ, maxY, r, g, b, a, packedLight, overlay);
      }

      if ((skipFaceMask & 1 << Direction.NORTH.ordinal()) == 0) {
         emitStaticPipeQuad(
            pose, buffer, sprite, Direction.NORTH, maxX, minY, minZ, maxX, maxY, minZ, minX, maxY, minZ, minX, minY, minZ, r, g, b, a, packedLight, overlay
         );
      }

      if ((skipFaceMask & 1 << Direction.SOUTH.ordinal()) == 0) {
         emitStaticPipeQuad(
            pose, buffer, sprite, Direction.SOUTH, minX, minY, maxZ, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, minY, maxZ, r, g, b, a, packedLight, overlay
         );
      }

      if ((skipFaceMask & 1 << Direction.WEST.ordinal()) == 0) {
         emitStaticPipeQuad(
            pose, buffer, sprite, Direction.WEST, minX, minY, minZ, minX, maxY, minZ, minX, maxY, maxZ, minX, minY, maxZ, r, g, b, a, packedLight, overlay
         );
      }

      if ((skipFaceMask & 1 << Direction.EAST.ordinal()) == 0) {
         emitStaticPipeQuad(
            pose, buffer, sprite, Direction.EAST, maxX, minY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, maxX, minY, minZ, r, g, b, a, packedLight, overlay
         );
      }
   }

   private static void emitStaticPipeHorizontal(
      Pose pose,
      VertexConsumer buffer,
      TextureAtlasSprite sprite,
      Direction face,
      float x0,
      float x1,
      float z0,
      float z1,
      float y,
      float r,
      float g,
      float b,
      float a,
      int packedLight,
      int overlay
   ) {
      float nx = face.getStepX();
      float ny = face.getStepY();
      float nz = face.getStepZ();
      emitQuadWithAtlasUv(
         pose,
         buffer,
         sprite,
         null,
         1,
         x0,
         y,
         z0,
         sprite.getU(x0),
         sprite.getV(z0),
         x1,
         y,
         z0,
         sprite.getU(x1),
         sprite.getV(z0),
         x1,
         y,
         z1,
         sprite.getU(x1),
         sprite.getV(z1),
         x0,
         y,
         z1,
         sprite.getU(x0),
         sprite.getV(z1),
         nx,
         ny,
         nz,
         r,
         g,
         b,
         a,
         packedLight,
         overlay
      );
   }

   private static void emitStaticPipeQuad(
      Pose pose,
      VertexConsumer buffer,
      TextureAtlasSprite sprite,
      Direction face,
      float x1,
      float y1,
      float z1,
      float x2,
      float y2,
      float z2,
      float x3,
      float y3,
      float z3,
      float x4,
      float y4,
      float z4,
      float r,
      float g,
      float b,
      float a,
      int packedLight,
      int overlay
   ) {
      float nx = face.getStepX();
      float ny = face.getStepY();
      float nz = face.getStepZ();
      emitQuadWithAtlasUv(
         pose,
         buffer,
         sprite,
         null,
         1,
         x1,
         y1,
         z1,
         posU(sprite, nx, x1, z1),
         posV(sprite, y1),
         x2,
         y2,
         z2,
         posU(sprite, nx, x2, z2),
         posV(sprite, y2),
         x3,
         y3,
         z3,
         posU(sprite, nx, x3, z3),
         posV(sprite, y3),
         x4,
         y4,
         z4,
         posU(sprite, nx, x4, z4),
         posV(sprite, y4),
         nx,
         ny,
         nz,
         r,
         g,
         b,
         a,
         packedLight,
         overlay
      );
   }

   public static void emitPipeCuboid(
      Pose pose,
      VertexConsumer buffer,
      TextureAtlasSprite sprite,
      BCEnergyFluidsFabric.FluidEntry entry,
      float minX,
      float minY,
      float minZ,
      float maxX,
      float maxY,
      float maxZ,
      int skipFaceMask,
      float r,
      float g,
      float b,
      float a,
      int packedLight
   ) {
      int overlay = OverlayTexture.NO_OVERLAY;
      int subdiv = entry != null ? 8 : 1;
      float texDiffX = pipeTexDiff(minX);
      float texDiffY = pipeTexDiff(minY);
      float texDiffZ = pipeTexDiff(minZ);
      if ((skipFaceMask & 1 << Direction.DOWN.ordinal()) == 0) {
         emitPipeHorizontal(
            pose, buffer, sprite, entry, subdiv, Direction.DOWN, minX, maxX, maxZ, minZ, minY, texDiffX, texDiffY, texDiffZ, r, g, b, a, packedLight, overlay
         );
      }

      if ((skipFaceMask & 1 << Direction.UP.ordinal()) == 0) {
         emitPipeHorizontal(
            pose, buffer, sprite, entry, subdiv, Direction.UP, minX, maxX, maxZ, minZ, maxY, texDiffX, texDiffY, texDiffZ, r, g, b, a, packedLight, overlay
         );
      }

      if ((skipFaceMask & 1 << Direction.NORTH.ordinal()) == 0) {
         emitPipeQuad(
            pose,
            buffer,
            sprite,
            entry,
            subdiv,
            Direction.NORTH,
            maxX,
            maxY,
            minZ,
            maxX,
            minY,
            minZ,
            minX,
            minY,
            minZ,
            minX,
            maxY,
            minZ,
            texDiffX,
            texDiffY,
            texDiffZ,
            r,
            g,
            b,
            a,
            packedLight,
            overlay
         );
      }

      if ((skipFaceMask & 1 << Direction.SOUTH.ordinal()) == 0) {
         emitPipeQuad(
            pose,
            buffer,
            sprite,
            entry,
            subdiv,
            Direction.SOUTH,
            minX,
            maxY,
            maxZ,
            minX,
            minY,
            maxZ,
            maxX,
            minY,
            maxZ,
            maxX,
            maxY,
            maxZ,
            texDiffX,
            texDiffY,
            texDiffZ,
            r,
            g,
            b,
            a,
            packedLight,
            overlay
         );
      }

      if ((skipFaceMask & 1 << Direction.WEST.ordinal()) == 0) {
         emitPipeQuad(
            pose,
            buffer,
            sprite,
            entry,
            subdiv,
            Direction.WEST,
            minX,
            maxY,
            minZ,
            minX,
            minY,
            minZ,
            minX,
            minY,
            maxZ,
            minX,
            maxY,
            maxZ,
            texDiffX,
            texDiffY,
            texDiffZ,
            r,
            g,
            b,
            a,
            packedLight,
            overlay
         );
      }

      if ((skipFaceMask & 1 << Direction.EAST.ordinal()) == 0) {
         emitPipeQuad(
            pose,
            buffer,
            sprite,
            entry,
            subdiv,
            Direction.EAST,
            maxX,
            maxY,
            maxZ,
            maxX,
            minY,
            maxZ,
            maxX,
            minY,
            minZ,
            maxX,
            maxY,
            minZ,
            texDiffX,
            texDiffY,
            texDiffZ,
            r,
            g,
            b,
            a,
            packedLight,
            overlay
         );
      }
   }

   public static void emitPipeQuad(
      Pose pose,
      VertexConsumer buffer,
      TextureAtlasSprite sprite,
      BCEnergyFluidsFabric.FluidEntry entry,
      int subdivisions,
      Direction face,
      float x1,
      float y1,
      float z1,
      float x2,
      float y2,
      float z2,
      float x3,
      float y3,
      float z3,
      float x4,
      float y4,
      float z4,
      float texDiffX,
      float texDiffY,
      float texDiffZ,
      float r,
      float g,
      float b,
      float a,
      int packedLight,
      int overlay
   ) {
      float nx = face.getStepX();
      float ny = face.getStepY();
      float nz = face.getStepZ();
      emitQuadWithAtlasUv(
         pose,
         buffer,
         sprite,
         entry,
         subdivisions,
         x1,
         y1,
         z1,
         pipeFaceU(sprite, face, x1, y1, z1, texDiffX, texDiffY, texDiffZ),
         pipeFaceV(sprite, face, x1, y1, z1, texDiffX, texDiffY, texDiffZ),
         x2,
         y2,
         z2,
         pipeFaceU(sprite, face, x2, y2, z2, texDiffX, texDiffY, texDiffZ),
         pipeFaceV(sprite, face, x2, y2, z2, texDiffX, texDiffY, texDiffZ),
         x3,
         y3,
         z3,
         pipeFaceU(sprite, face, x3, y3, z3, texDiffX, texDiffY, texDiffZ),
         pipeFaceV(sprite, face, x3, y3, z3, texDiffX, texDiffY, texDiffZ),
         x4,
         y4,
         z4,
         pipeFaceU(sprite, face, x4, y4, z4, texDiffX, texDiffY, texDiffZ),
         pipeFaceV(sprite, face, x4, y4, z4, texDiffX, texDiffY, texDiffZ),
         nx,
         ny,
         nz,
         r,
         g,
         b,
         a,
         packedLight,
         overlay
      );
   }

   public static void emitPipeHorizontal(
      Pose pose,
      VertexConsumer buffer,
      TextureAtlasSprite sprite,
      BCEnergyFluidsFabric.FluidEntry entry,
      int subdivisions,
      Direction face,
      float x0,
      float x1,
      float z0,
      float z1,
      float y,
      float texDiffX,
      float texDiffY,
      float texDiffZ,
      float r,
      float g,
      float b,
      float a,
      int packedLight,
      int overlay
   ) {
      float nx = face.getStepX();
      float ny = face.getStepY();
      float nz = face.getStepZ();
      emitQuadWithAtlasUv(
         pose,
         buffer,
         sprite,
         entry,
         subdivisions,
         x0,
         y,
         z0,
         pipeFaceU(sprite, face, x0, y, z0, texDiffX, texDiffY, texDiffZ),
         pipeFaceV(sprite, face, x0, y, z0, texDiffX, texDiffY, texDiffZ),
         x1,
         y,
         z0,
         pipeFaceU(sprite, face, x1, y, z0, texDiffX, texDiffY, texDiffZ),
         pipeFaceV(sprite, face, x1, y, z0, texDiffX, texDiffY, texDiffZ),
         x1,
         y,
         z1,
         pipeFaceU(sprite, face, x1, y, z1, texDiffX, texDiffY, texDiffZ),
         pipeFaceV(sprite, face, x1, y, z1, texDiffX, texDiffY, texDiffZ),
         x0,
         y,
         z1,
         pipeFaceU(sprite, face, x0, y, z1, texDiffX, texDiffY, texDiffZ),
         pipeFaceV(sprite, face, x0, y, z1, texDiffX, texDiffY, texDiffZ),
         nx,
         ny,
         nz,
         r,
         g,
         b,
         a,
         packedLight,
         overlay
      );
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

   private static void emitQuadWithAtlasUv(
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

   private static void putVertex(
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

   private static float bilinear(float c0, float c1, float c2, float c3, float u, float v) {
      float top = lerp(c0, c1, u);
      float bot = lerp(c3, c2, u);
      return lerp(top, bot, v);
   }

   private static float lerp(float a, float b, float t) {
      return a + (b - a) * t;
   }

   public static float posU(TextureAtlasSprite sprite, float nx, float x, float z) {
      return sprite.getU(nx != 0.0F ? z : x);
   }

   public static float posV(TextureAtlasSprite sprite, float y) {
      return sprite.getV(1.0F - y);
   }

   static float pipeTexDiff(float coord) {
      if (coord > 1.0F) {
         return (float)Math.floor(coord);
      } else {
         return coord < 0.0F ? (float)Math.floor(coord) : 0.0F;
      }
   }

   private static float pipeFrozenCoord(float shifted) {
      return shifted - (float)Math.floor(shifted);
   }

   static float pipeFaceU(TextureAtlasSprite sprite, Direction face, float x, float y, float z, float texDiffX, float texDiffY, float texDiffZ) {
      float shifted = switch (face) {
         case UP, DOWN, NORTH, SOUTH -> x - texDiffX;
         case EAST, WEST -> z - texDiffZ;
         default -> x - texDiffX;
      };
      return sprite.getU(pipeFrozenCoord(shifted));
   }

   static float pipeFaceV(TextureAtlasSprite sprite, Direction face, float x, float y, float z, float texDiffX, float texDiffY, float texDiffZ) {
      float shifted = switch (face) {
         case UP, DOWN -> z - texDiffZ;
         case NORTH, SOUTH, EAST, WEST -> y - texDiffY;
         default -> y - texDiffY;
      };
      return sprite.getV(pipeFrozenCoord(shifted));
   }
}

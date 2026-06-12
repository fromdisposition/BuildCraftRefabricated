/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.fluid;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

public final class BcFluidPipeQuads {
   private BcFluidPipeQuads() {
   }

   public static void emitPipeCuboid(
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
      float[] rgba,
      int packedLight
   ) {
      int overlay = OverlayTexture.NO_OVERLAY;
      float r = rgba[0];
      float g = rgba[1];
      float b = rgba[2];
      float a = rgba[3];
      if ((skipFaceMask & 1 << Direction.DOWN.ordinal()) == 0) {
         emitPipeHorizontal(pose, buffer, sprite, Direction.DOWN, minX, maxX, maxZ, minZ, minY, r, g, b, a, packedLight, overlay);
      }

      if ((skipFaceMask & 1 << Direction.UP.ordinal()) == 0) {
         emitPipeHorizontal(pose, buffer, sprite, Direction.UP, minX, maxX, maxZ, minZ, maxY, r, g, b, a, packedLight, overlay);
      }

      if ((skipFaceMask & 1 << Direction.NORTH.ordinal()) == 0) {
         emitPipeQuad(
            pose, buffer, sprite, Direction.NORTH, maxX, maxY, minZ, maxX, minY, minZ, minX, minY, minZ, minX, maxY, minZ, r, g, b, a, packedLight, overlay
         );
      }

      if ((skipFaceMask & 1 << Direction.SOUTH.ordinal()) == 0) {
         emitPipeQuad(
            pose, buffer, sprite, Direction.SOUTH, minX, maxY, maxZ, minX, minY, maxZ, maxX, minY, maxZ, maxX, maxY, maxZ, r, g, b, a, packedLight, overlay
         );
      }

      if ((skipFaceMask & 1 << Direction.WEST.ordinal()) == 0) {
         emitPipeQuad(
            pose, buffer, sprite, Direction.WEST, minX, maxY, minZ, minX, minY, minZ, minX, minY, maxZ, minX, maxY, maxZ, r, g, b, a, packedLight, overlay
         );
      }

      if ((skipFaceMask & 1 << Direction.EAST.ordinal()) == 0) {
         emitPipeQuad(
            pose, buffer, sprite, Direction.EAST, maxX, maxY, maxZ, maxX, minY, maxZ, maxX, minY, minZ, maxX, maxY, minZ, r, g, b, a, packedLight, overlay
         );
      }
   }

   private static void emitPipeQuad(
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
      BcFluidVertexEmitter.emitQuadWithAtlasUv(
         pose,
         buffer,
         sprite,
         null,
         1,
         x1,
         y1,
         z1,
         faceU(sprite, face, x1, y1, z1),
         faceV(sprite, face, x1, y1, z1),
         x2,
         y2,
         z2,
         faceU(sprite, face, x2, y2, z2),
         faceV(sprite, face, x2, y2, z2),
         x3,
         y3,
         z3,
         faceU(sprite, face, x3, y3, z3),
         faceV(sprite, face, x3, y3, z3),
         x4,
         y4,
         z4,
         faceU(sprite, face, x4, y4, z4),
         faceV(sprite, face, x4, y4, z4),
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

   private static void emitPipeHorizontal(
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
      BcFluidVertexEmitter.emitQuadWithAtlasUv(
         pose,
         buffer,
         sprite,
         null,
         1,
         x0,
         y,
         z0,
         faceU(sprite, face, x0, y, z0),
         faceV(sprite, face, x0, y, z0),
         x1,
         y,
         z0,
         faceU(sprite, face, x1, y, z0),
         faceV(sprite, face, x1, y, z0),
         x1,
         y,
         z1,
         faceU(sprite, face, x1, y, z1),
         faceV(sprite, face, x1, y, z1),
         x0,
         y,
         z1,
         faceU(sprite, face, x0, y, z1),
         faceV(sprite, face, x0, y, z1),
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

   private static float faceU(TextureAtlasSprite sprite, Direction face, float x, float y, float z) {
      float coord = switch (face) {
         case UP, DOWN, NORTH, SOUTH -> x;
         case EAST, WEST -> z;
         default -> x;
      };
      return sprite.getU(coord);
   }

   private static float faceV(TextureAtlasSprite sprite, Direction face, float x, float y, float z) {
      float coord = switch (face) {
         case UP, DOWN -> z;
         case NORTH, SOUTH, EAST, WEST -> y;
         default -> y;
      };
      return sprite.getV(coord);
   }
}

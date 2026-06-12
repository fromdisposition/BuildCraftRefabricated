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

public final class BcFluidStaticPipeQuads {
   private BcFluidStaticPipeQuads() {
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
      BcFluidVertexEmitter.emitQuadWithAtlasUv(
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
      BcFluidVertexEmitter.emitQuadWithAtlasUv(
         pose,
         buffer,
         sprite,
         null,
         1,
         x1,
         y1,
         z1,
         BcFluidVertexEmitter.posU(sprite, nx, x1, z1),
         BcFluidVertexEmitter.posV(sprite, y1),
         x2,
         y2,
         z2,
         BcFluidVertexEmitter.posU(sprite, nx, x2, z2),
         BcFluidVertexEmitter.posV(sprite, y2),
         x3,
         y3,
         z3,
         BcFluidVertexEmitter.posU(sprite, nx, x3, z3),
         BcFluidVertexEmitter.posV(sprite, y3),
         x4,
         y4,
         z4,
         BcFluidVertexEmitter.posU(sprite, nx, x4, z4),
         BcFluidVertexEmitter.posV(sprite, y4),
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
}

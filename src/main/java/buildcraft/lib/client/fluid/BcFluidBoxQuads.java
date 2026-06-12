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
import org.jspecify.annotations.Nullable;

/** Shared flat fluid cuboid quads for tanks, pipes, and other BER fluid boxes. */
public final class BcFluidBoxQuads {
   private BcFluidBoxQuads() {
   }

   public static void emitCuboid(
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
         emitHorizontal(pose, buffer, sprite, minX, maxX, maxZ, minZ, minY, 0.0F, -1.0F, 0.0F, r, g, b, a, packedLight, overlay);
      }

      if ((skipFaceMask & 1 << Direction.UP.ordinal()) == 0) {
         emitHorizontal(pose, buffer, sprite, minX, maxX, maxZ, minZ, maxY, 0.0F, 1.0F, 0.0F, r, g, b, a, packedLight, overlay);
      }

      if ((skipFaceMask & 1 << Direction.NORTH.ordinal()) == 0) {
         emitDirectionalQuad(
            pose, buffer, sprite, Direction.NORTH, maxX, maxY, minZ, maxX, minY, minZ, minX, minY, minZ, minX, maxY, minZ, r, g, b, a, packedLight, overlay
         );
      }

      if ((skipFaceMask & 1 << Direction.SOUTH.ordinal()) == 0) {
         emitDirectionalQuad(
            pose, buffer, sprite, Direction.SOUTH, minX, maxY, maxZ, minX, minY, maxZ, maxX, minY, maxZ, maxX, maxY, maxZ, r, g, b, a, packedLight, overlay
         );
      }

      if ((skipFaceMask & 1 << Direction.WEST.ordinal()) == 0) {
         emitDirectionalQuad(
            pose, buffer, sprite, Direction.WEST, minX, maxY, minZ, minX, minY, minZ, minX, minY, maxZ, minX, maxY, maxZ, r, g, b, a, packedLight, overlay
         );
      }

      if ((skipFaceMask & 1 << Direction.EAST.ordinal()) == 0) {
         emitDirectionalQuad(
            pose, buffer, sprite, Direction.EAST, maxX, maxY, maxZ, maxX, minY, maxZ, maxX, minY, minZ, maxX, maxY, minZ, r, g, b, a, packedLight, overlay
         );
      }
   }

   public static void emitCuboid(
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
      emitCuboid(pose, buffer, sprite, minX, minY, minZ, maxX, maxY, maxZ, skipFaceMask, rgba[0], rgba[1], rgba[2], rgba[3], packedLight);
   }

   public static void emitHorizontal(
      Pose pose,
      VertexConsumer buffer,
      TextureAtlasSprite sprite,
      float x0,
      float x1,
      float z0,
      float z1,
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
      BcFluidVertexEmitter.emitQuadWithAtlasUv(
         pose,
         buffer,
         sprite,
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
         light,
         overlay
      );
   }

   public static void emitNormalQuad(
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
      emitQuad(
         pose,
         buffer,
         sprite,
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
         nx,
         ny,
         nz,
         null,
         r,
         g,
         b,
         a,
         light,
         overlay
      );
   }

   private static void emitDirectionalQuad(
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
      emitQuad(
         pose,
         buffer,
         sprite,
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
         face.getStepX(),
         face.getStepY(),
         face.getStepZ(),
         face,
         r,
         g,
         b,
         a,
         packedLight,
         overlay
      );
   }

   private static void emitQuad(
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
      @Nullable Direction pipeFace,
      float r,
      float g,
      float b,
      float a,
      int light,
      int overlay
   ) {
      BcFluidVertexEmitter.emitQuadWithAtlasUv(
         pose,
         buffer,
         sprite,
         x1,
         y1,
         z1,
         atlasU(sprite, pipeFace, nx, x1, y1, z1),
         atlasV(sprite, pipeFace, nx, ny, x1, y1, z1),
         x2,
         y2,
         z2,
         atlasU(sprite, pipeFace, nx, x2, y2, z2),
         atlasV(sprite, pipeFace, nx, ny, x2, y2, z2),
         x3,
         y3,
         z3,
         atlasU(sprite, pipeFace, nx, x3, y3, z3),
         atlasV(sprite, pipeFace, nx, ny, x3, y3, z3),
         x4,
         y4,
         z4,
         atlasU(sprite, pipeFace, nx, x4, y4, z4),
         atlasV(sprite, pipeFace, nx, ny, x4, y4, z4),
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

   private static float atlasU(TextureAtlasSprite sprite, @Nullable Direction pipeFace, float nx, float x, float y, float z) {
      if (pipeFace != null) {
         float coord = switch (pipeFace) {
            case UP, DOWN, NORTH, SOUTH -> x;
            case EAST, WEST -> z;
            default -> x;
         };
         return sprite.getU(coord);
      }

      return BcFluidVertexEmitter.posU(sprite, nx, x, z);
   }

   private static float atlasV(TextureAtlasSprite sprite, @Nullable Direction pipeFace, float nx, float ny, float x, float y, float z) {
      if (pipeFace != null) {
         float coord = switch (pipeFace) {
            case UP, DOWN -> z;
            case NORTH, SOUTH, EAST, WEST -> y;
            default -> y;
         };
         return sprite.getV(coord);
      }

      return BcFluidVertexEmitter.posV(sprite, y);
   }
}

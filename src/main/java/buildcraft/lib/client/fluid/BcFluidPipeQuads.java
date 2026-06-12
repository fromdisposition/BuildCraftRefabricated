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

public final class BcFluidPipeQuads {
   private BcFluidPipeQuads() {
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

   static void emitPipeQuad(
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
      BcFluidVertexEmitter.emitQuadWithAtlasUv(
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

   static void emitPipeHorizontal(
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
      BcFluidVertexEmitter.emitQuadWithAtlasUv(
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

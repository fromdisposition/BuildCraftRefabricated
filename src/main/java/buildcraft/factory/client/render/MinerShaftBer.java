/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.client.render;

import buildcraft.factory.tile.TileMiner;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

final class MinerShaftBer {
   private static final float TEX = 16.0F;
   private static final float CROSS = 8.0F;
   private static final float CAP = 8.0F;
   private static final float MIDDLE = 16.0F;
   private static final float SCALE = (float)TileMiner.SHAFT_RADIUS;
   private static final float R = CROSS * 0.5F * SCALE;
   private static final float Y0 = -0.001F;
   private static final float C = 0.5F;

   private MinerShaftBer() {
   }

   static boolean shouldRenderOffScreen() {
      return true;
   }

   static int getViewDistance() {
      return Minecraft.getInstance().options.getEffectiveRenderDistance() * 16;
   }

   static boolean shouldRender(BlockEntity blockEntity, Vec3 cameraPos) {
      return Vec3.atCenterOf(blockEntity.getBlockPos())
         .multiply(1.0, 0.0, 1.0)
         .closerThan(cameraPos.multiply(1.0, 0.0, 1.0), getViewDistance());
   }

   static void renderShaft(Pose pose, VertexConsumer buffer, BlockPos blockPos, TextureAtlasSprite sprite, float length) {
      if (length <= 0.0F) {
         return;
      }

      Level level = Minecraft.getInstance().level;
      int overlay = OverlayTexture.NO_OVERLAY;
      float lengthPx = length / SCALE;
      float middleWidth = MIDDLE;
      float lengthForMiddle = Math.max(0.0F, lengthPx - middleWidth);
      int numMiddle = Mth.floor(lengthForMiddle / middleWidth);
      if (lengthForMiddle - numMiddle * middleWidth > 0.001F) {
         numMiddle++;
      }

      float startLength = lengthPx - middleWidth * numMiddle;
      float lx = 0.0F;
      if (startLength > 0.001F) {
         float u0 = uvU(sprite, MIDDLE * (1.0F - startLength / middleWidth));
         sides(pose, buffer, blockPos, level, sprite, lx, lx + startLength, u0, uvU(sprite, MIDDLE), overlay);
         lx += startLength;
      }

      for (int i = 0; i < numMiddle; i++) {
         sides(pose, buffer, blockPos, level, sprite, lx, lx + middleWidth, uvU(sprite, 0.0F), uvU(sprite, MIDDLE), overlay);
         lx += middleWidth;
      }

      float bottomY = y(lengthPx);
      cap(pose, buffer, blockPos, level, sprite, Y0, true, overlay);
      cap(pose, buffer, blockPos, level, sprite, bottomY, false, overlay);
   }

   private static void sides(
      Pose pose,
      VertexConsumer buffer,
      BlockPos origin,
      Level level,
      TextureAtlasSprite sprite,
      float lx0,
      float lx1,
      float u0,
      float u1,
      int overlay
   ) {
      float y0 = y(lx0);
      float y1 = y(lx1);
      float vs = uvV(sprite, 0.0F);
      float ve = uvV(sprite, CROSS);
      float xm = C - R;
      float xM = C + R;
      float zm = C - R;
      float zM = C + R;
      quad(pose, buffer, origin, level, xm, y0, zm, xM, y0, zm, xM, y1, zm, xm, y1, zm, 0, 0, -1, u0, vs, u0, ve, u1, ve, u1, vs, overlay);
      quad(pose, buffer, origin, level, xm, y1, zM, xM, y1, zM, xM, y0, zM, xm, y0, zM, 0, 0, 1, u1, vs, u1, ve, u0, ve, u0, vs, overlay);
      quad(pose, buffer, origin, level, xm, y0, zM, xm, y0, zm, xm, y1, zm, xm, y1, zM, -1, 0, 0, u0, ve, u0, vs, u1, vs, u1, ve, overlay);
      quad(pose, buffer, origin, level, xM, y0, zm, xM, y0, zM, xM, y1, zM, xM, y1, zm, 1, 0, 0, u0, vs, u0, ve, u1, ve, u1, vs, overlay);
   }

   private static void cap(Pose pose, VertexConsumer buffer, BlockPos origin, Level level, TextureAtlasSprite sprite, float y, boolean top, int overlay) {
      float xm = C - R;
      float xM = C + R;
      float zm = C - R;
      float zM = C + R;
      float u0 = uvU(sprite, 0.0F);
      float u1 = uvU(sprite, CAP);
      float v0 = uvV(sprite, CAP);
      float v1 = uvV(sprite, CAP + CROSS);

      if (top) {
         quad(pose, buffer, origin, level, xm, y, zM, xM, y, zM, xM, y, zm, xm, y, zm, 0, 1, 0, u0, v0, u1, v0, u1, v1, u0, v1, overlay);
      } else {
         quad(pose, buffer, origin, level, xm, y, zm, xM, y, zm, xM, y, zM, xm, y, zM, 0, -1, 0, u0, v1, u1, v1, u1, v0, u0, v0, overlay);
      }
   }

   private static float y(float alongPx) {
      float alongBlocks = alongPx * SCALE;
      return alongBlocks <= 0.0F ? Y0 : -alongBlocks;
   }

   private static float uvU(TextureAtlasSprite sprite, float px) {
      return sprite.getU(px / TEX);
   }

   private static float uvV(TextureAtlasSprite sprite, float px) {
      return sprite.getV(px / TEX);
   }

   private static void quad(
      Pose pose,
      VertexConsumer buffer,
      BlockPos origin,
      Level level,
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
      float u1,
      float v1,
      float u2,
      float v2,
      float u3,
      float v3,
      float u4,
      float v4,
      int overlay
   ) {
      vertex(pose, buffer, origin, level, x1, y1, z1, u1, v1, nx, ny, nz, overlay);
      vertex(pose, buffer, origin, level, x2, y2, z2, u2, v2, nx, ny, nz, overlay);
      vertex(pose, buffer, origin, level, x3, y3, z3, u3, v3, nx, ny, nz, overlay);
      vertex(pose, buffer, origin, level, x4, y4, z4, u4, v4, nx, ny, nz, overlay);
   }

   private static void vertex(
      Pose pose,
      VertexConsumer buffer,
      BlockPos origin,
      Level level,
      float x,
      float y,
      float z,
      float u,
      float v,
      float nx,
      float ny,
      float nz,
      int overlay
   ) {
      buffer.addVertex(pose, x, y, z)
         .setColor(255, 255, 255, 255)
         .setUv(u, v)
         .setOverlay(overlay)
         .setLight(shaftLight(origin, level, y))
         .setNormal(pose, nx, ny, nz);
   }

   /** Vanilla BER light query: {@link LevelRenderer#getLightCoords} at the block cell center. */
   private static int shaftLight(BlockPos origin, Level level, float localY) {
      if (level == null) {
         return LightCoordsUtil.FULL_BRIGHT;
      }

      double sampleY = origin.getY() + localY + 0.5D;
      if (Mth.floor(sampleY) >= origin.getY()) {
         sampleY = origin.getY() - 0.5D;
      }

      return LevelRenderer.getLightCoords(level, BlockPos.containing(origin.getX() + C, sampleY, origin.getZ() + C));
   }
}

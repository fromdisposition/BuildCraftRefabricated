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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

final class MinerShaftBer {
   private static final float CAP_HEIGHT = 0.5F;
   private static final float MIDDLE_HEIGHT = 1.0F;

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

   static int[] computeSegmentLights(Level level, BlockPos pumpPos, double shaftLength) {
      if (level == null || shaftLength <= 0.0) {
         return new int[0];
      }

      int count = (int)Math.ceil(shaftLength) + 1;
      int[] lights = new int[count];

      for (int i = 0; i < count; i++) {
         lights[i] = LevelRenderer.getLightCoords(level, pumpPos.below(i));
      }

      return lights;
   }

   static void renderShaft(Pose pose, VertexConsumer buffer, TextureAtlasSprite sprite, float length, int[] segmentLights) {
      float r = (float)TileMiner.SHAFT_RADIUS;
      float cx = 0.5F;
      float cz = 0.5F;
      float yTop = -0.001F;
      float yBottom = -length;
      int overlay = OverlayTexture.NO_OVERLAY;
      float y = yTop;
      float capEnd = yTop - CAP_HEIGHT;
      renderShaftSegment(pose, buffer, sprite, cx, cz, r, y, capEnd, segmentLights, overlay);
      y = capEnd;
      float bodyEnd = yBottom + CAP_HEIGHT;

      while (y > bodyEnd + 0.001F) {
         float segEnd = Math.max(bodyEnd, y - MIDDLE_HEIGHT);
         renderShaftSegment(pose, buffer, sprite, cx, cz, r, y, segEnd, segmentLights, overlay);
         y = segEnd;
      }

      renderShaftSegment(pose, buffer, sprite, cx, cz, r, bodyEnd, yBottom, segmentLights, overlay);
   }

   private static void renderShaftSegment(
      Pose pose,
      VertexConsumer buffer,
      TextureAtlasSprite sprite,
      float cx,
      float cz,
      float r,
      float yTop,
      float yBottom,
      int[] segmentLights,
      int overlay
   ) {
      float u0 = spriteU(sprite, 0.0F);
      float u1 = spriteU(sprite, 8.0F);
      float v0 = spriteV(sprite, 8.0F);
      float v1 = spriteV(sprite, 16.0F);
      if (yTop - yBottom > CAP_HEIGHT + 0.001F) {
         u0 = spriteU(sprite, 0.0F);
         u1 = spriteU(sprite, 16.0F);
         v0 = spriteV(sprite, 0.0F);
         v1 = spriteV(sprite, 8.0F);
      }

      int lightTop = lightAt(yTop, segmentLights);
      int lightBottom = lightAt(yBottom, segmentLights);
      float xMin = cx - r;
      float xMax = cx + r;
      float zMin = cz - r;
      float zMax = cz + r;
      shaftFace(
         pose, buffer, xMin, yTop, zMin, xMax, yTop, zMin, xMax, yBottom, zMin, xMin, yBottom, zMin,
         0.0F, 0.0F, -1.0F, u0, v0, u1, v0, u1, v1, u0, v1, lightTop, lightBottom, overlay
      );
      shaftFace(
         pose, buffer, xMin, yBottom, zMax, xMax, yBottom, zMax, xMax, yTop, zMax, xMin, yTop, zMax,
         0.0F, 0.0F, 1.0F, u0, v1, u1, v1, u1, v0, u0, v0, lightBottom, lightTop, overlay
      );
      shaftFace(
         pose, buffer, xMin, yTop, zMax, xMin, yTop, zMin, xMin, yBottom, zMin, xMin, yBottom, zMax,
         -1.0F, 0.0F, 0.0F, u0, v0, u0, v1, u1, v1, u1, v0, lightTop, lightBottom, overlay
      );
      shaftFace(
         pose, buffer, xMax, yTop, zMin, xMax, yTop, zMax, xMax, yBottom, zMax, xMax, yBottom, zMin,
         1.0F, 0.0F, 0.0F, u0, v0, u1, v0, u1, v1, u0, v1, lightTop, lightBottom, overlay
      );
   }

   private static int lightAt(float localY, int[] segmentLights) {
      if (segmentLights.length == 0) {
         return 15728880;
      }

      int blockDepth = Math.max(0, (int)Math.ceil(-localY - 0.001F));
      return segmentLights[Math.min(blockDepth, segmentLights.length - 1)];
   }

   private static void shaftFace(
      Pose pose,
      VertexConsumer buffer,
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
      int lightTop,
      int lightBottom,
      int overlay
   ) {
      int light1 = y1 >= y3 ? lightTop : lightBottom;
      int light2 = y2 >= y4 ? lightTop : lightBottom;
      int light3 = y3 >= y1 ? lightBottom : lightTop;
      int light4 = y4 >= y2 ? lightBottom : lightTop;
      buffer.addVertex(pose, x1, y1, z1).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(u1, v1).setOverlay(overlay).setLight(light1).setNormal(pose, nx, ny, nz);
      buffer.addVertex(pose, x2, y2, z2).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(u2, v2).setOverlay(overlay).setLight(light2).setNormal(pose, nx, ny, nz);
      buffer.addVertex(pose, x3, y3, z3).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(u3, v3).setOverlay(overlay).setLight(light3).setNormal(pose, nx, ny, nz);
      buffer.addVertex(pose, x4, y4, z4).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(u4, v4).setOverlay(overlay).setLight(light4).setNormal(pose, nx, ny, nz);
   }

   private static float spriteU(TextureAtlasSprite sprite, float px) {
      return sprite.getU0() + (sprite.getU1() - sprite.getU0()) * (px / 16.0F);
   }

   private static float spriteV(TextureAtlasSprite sprite, float py) {
      return sprite.getV0() + (sprite.getV1() - sprite.getV0()) * (py / 16.0F);
   }
}

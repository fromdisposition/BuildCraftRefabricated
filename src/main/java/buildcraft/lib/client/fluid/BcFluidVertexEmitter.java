/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.fluid;

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
      putVertex(pose, buffer, x1, y1, z1, au1, av1, nx, ny, nz, r, g, b, a, light, overlay, false);
      putVertex(pose, buffer, x2, y2, z2, au2, av2, nx, ny, nz, r, g, b, a, light, overlay, false);
      putVertex(pose, buffer, x3, y3, z3, au3, av3, nx, ny, nz, r, g, b, a, light, overlay, false);
      putVertex(pose, buffer, x4, y4, z4, au4, av4, nx, ny, nz, r, g, b, a, light, overlay, false);
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

   static float posU(TextureAtlasSprite sprite, float nx, float x, float z) {
      return sprite.getU(nx != 0.0F ? z : x);
   }

   static float posV(TextureAtlasSprite sprite, float y) {
      return sprite.getV(1.0F - y);
   }
}

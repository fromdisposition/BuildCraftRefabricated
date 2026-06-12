/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.fluid;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public final class BcFluidTankQuads {
   private BcFluidTankQuads() {
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
      BcFluidVertexEmitter.emitQuadWithAtlasUv(
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
}

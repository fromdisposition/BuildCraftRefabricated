/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.fluid;


import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import buildcraft.lib.fluid.stack.FluidStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public final class BcFluidTankRenderer {
   private BcFluidTankRenderer() {
   }

   public static void renderFilledBox(
      Pose pose,
      VertexConsumer buffer,
      TextureAtlasSprite sprite,
      float minX,
      float minY,
      float minZ,
      float maxX,
      float maxY,
      float maxZ,
      FluidStack fluid,
      double amount,
      int capacity,
      boolean renderTop,
      boolean renderBottom,
      int light,
      int overlay
   ) {
      if (!(amount <= 0.0) && capacity > 0 && fluid != null && !fluid.isEmpty()) {
         float[] rgba = BcFluidRenderLookup.vertexRgba(fluid);
         float r = rgba[0];
         float g = rgba[1];
         float b = rgba[2];
         float a = rgba[3];
         float fillRatio = (float)(amount / capacity);
         boolean gaseous = !fluid.isEmpty() && FluidVariantAttributes.isLighterThanAir(FluidVariant.of(fluid.getFluid()));
         float fluidTop;
         float fluidBottom;
         if (gaseous) {
            fluidTop = maxY;
            fluidBottom = maxY - (maxY - minY) * fillRatio;
         } else {
            fluidBottom = minY;
            fluidTop = minY + (maxY - minY) * fillRatio;
         }

         BcFluidBoxQuads.emitNormalQuad(
            pose,
            buffer,
            sprite,
            minX,
            fluidTop,
            minZ,
            maxX,
            fluidTop,
            minZ,
            maxX,
            fluidBottom,
            minZ,
            minX,
            fluidBottom,
            minZ,
            0.0F,
            0.0F,
            -1.0F,
            r,
            g,
            b,
            a,
            light,
            overlay
         );
         BcFluidBoxQuads.emitNormalQuad(
            pose,
            buffer,
            sprite,
            minX,
            fluidBottom,
            maxZ,
            maxX,
            fluidBottom,
            maxZ,
            maxX,
            fluidTop,
            maxZ,
            minX,
            fluidTop,
            maxZ,
            0.0F,
            0.0F,
            1.0F,
            r,
            g,
            b,
            a,
            light,
            overlay
         );
         BcFluidBoxQuads.emitNormalQuad(
            pose,
            buffer,
            sprite,
            minX,
            fluidBottom,
            minZ,
            minX,
            fluidBottom,
            maxZ,
            minX,
            fluidTop,
            maxZ,
            minX,
            fluidTop,
            minZ,
            -1.0F,
            0.0F,
            0.0F,
            r,
            g,
            b,
            a,
            light,
            overlay
         );
         BcFluidBoxQuads.emitNormalQuad(
            pose,
            buffer,
            sprite,
            maxX,
            fluidTop,
            minZ,
            maxX,
            fluidTop,
            maxZ,
            maxX,
            fluidBottom,
            maxZ,
            maxX,
            fluidBottom,
            minZ,
            1.0F,
            0.0F,
            0.0F,
            r,
            g,
            b,
            a,
            light,
            overlay
         );
         if (renderTop) {
            BcFluidBoxQuads.emitHorizontal(pose, buffer, sprite, minX, maxX, maxZ, minZ, fluidTop, 0.0F, 1.0F, 0.0F, r, g, b, a, light, overlay);
         }

         if (renderBottom) {
            BcFluidBoxQuads.emitHorizontal(pose, buffer, sprite, minX, maxX, minZ, maxZ, fluidBottom, 0.0F, -1.0F, 0.0F, r, g, b, a, light, overlay);
         }

         if (gaseous && fillRatio < 1.0F && renderBottom) {
            BcFluidBoxQuads.emitHorizontal(pose, buffer, sprite, minX, maxX, minZ, maxZ, fluidBottom, 0.0F, -1.0F, 0.0F, r, g, b, a, light, overlay);
         }
      }
   }
}

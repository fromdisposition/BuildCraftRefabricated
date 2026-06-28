/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.render.tile;

import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.engine.TileEngineBase_BC8;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.BiFunction;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;

/**
 * 1.21.1 (versions/1.21.1) engine renderer: immediate-mode port. The animated trunk/chamber quads come from
 * the version-neutral quad provider; they are drawn straight into the cutout block sheet.
 */
public class RenderEngine_BC8 implements BlockEntityRenderer<TileEngineBase_BC8> {
   private final BiFunction<TileEngineBase_BC8, Float, MutableQuad[]> quadProvider;

   public RenderEngine_BC8(BiFunction<TileEngineBase_BC8, Float, MutableQuad[]> quadProvider) {
      this.quadProvider = quadProvider;
   }

   @Override
   public void render(TileEngineBase_BC8 tile, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
      MutableQuad[] quads = this.quadProvider.apply(tile, partialTick);
      if (quads == null || quads.length == 0) {
         return;
      }
      poseStack.pushPose();
      VertexConsumer buffer = buffers.getBuffer(Sheets.cutoutBlockSheet());
      Pose pose = poseStack.last();
      for (MutableQuad quad : quads) {
         quad.setCalculatedDiffuse();
         quad.lighti(light);
         quad.render(pose, buffer);
      }
      poseStack.popPose();
   }
}

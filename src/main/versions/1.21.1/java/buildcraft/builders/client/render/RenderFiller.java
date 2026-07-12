/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.client.render;

import buildcraft.api.tiles.IControllable;
import buildcraft.builders.tile.TileFiller;
import buildcraft.lib.client.render.BCLibRenderTypes;
import buildcraft.lib.client.render.tile.LedRenderUtil;
import buildcraft.lib.client.render.tile.RenderPartCube;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;

/** 1.21.1 (versions/1.21.1) filler renderer: immediate-mode status LEDs (mode/power/finished). */
public class RenderFiller implements BlockEntityRenderer<TileFiller> {
   private static final RenderPartCube[] LED_GREEN = new RenderPartCube[4];
   private static final RenderPartCube[] LED_RED = new RenderPartCube[4];

   public RenderFiller(BlockEntityRendererProvider.Context context) {
   }

   @Override
   public void render(TileFiller tile, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
      IControllable.Mode controlMode = tile.getControlMode();
      boolean hasPower = tile.hasPower();
      boolean finished = tile.isFinished();
      int greenColour;
      int redColour;
      if (controlMode == IControllable.Mode.OFF) {
         greenColour = -14741477;
         redColour = -14741477;
      } else if (!hasPower) {
         greenColour = -14741477;
         redColour = -14540067;
      } else if (finished) {
         greenColour = -8921737;
         redColour = -14540067;
      } else if (controlMode == IControllable.Mode.LOOP) {
         greenColour = -12617921;
         redColour = -14741477;
      } else {
         greenColour = -8921737;
         redColour = -14741477;
      }

      poseStack.pushPose();
      VertexConsumer led = buffers.getBuffer(BCLibRenderTypes.led());
      Pose pose = poseStack.last();
      for (int i = 0; i < 4; i++) {
         Direction skipFace = Direction.from2DDataValue(i).getOpposite();
         LedRenderUtil.render(LED_GREEN[i], pose, led, skipFace, greenColour);
         LedRenderUtil.render(LED_RED[i], pose, led, skipFace, redColour);
      }
      poseStack.popPose();
   }

   static {
      for (int i = 0; i < 4; i++) {
         Direction face = Direction.from2DDataValue(i);
         LED_GREEN[i] = new RenderPartCube();
         LED_RED[i] = new RenderPartCube();
         LedRenderUtil.setFacePosition(LED_GREEN[i], face, 0.025, 0.09375, 0.84375);
         LedRenderUtil.setFacePosition(LED_RED[i], face, 0.025, 0.21875, 0.84375);
      }
   }
}

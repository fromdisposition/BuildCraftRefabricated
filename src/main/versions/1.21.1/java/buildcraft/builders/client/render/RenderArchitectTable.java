/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.client.render;

import buildcraft.builders.tile.TileArchitectTable;
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
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;

/** 1.21.1 (versions/1.21.1) architect-table renderer: immediate-mode status/validity LEDs. */
public class RenderArchitectTable implements BlockEntityRenderer<TileArchitectTable> {
   private static final int COLOUR_RED_HALF = -15658633;
   private static final RenderPartCube LED_GREEN = new RenderPartCube();
   private static final RenderPartCube LED_RED = new RenderPartCube();

   public RenderArchitectTable(BlockEntityRendererProvider.Context context) {
   }

   @Override
   public void render(TileArchitectTable tile, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
      BlockState blockState = tile.getBlockState();
      if (!blockState.hasProperty(HorizontalDirectionalBlock.FACING)) {
         return;
      }
      Direction facing = blockState.getValue(HorizontalDirectionalBlock.FACING);
      Direction skipFace = facing.getOpposite();
      boolean valid = tile.getIsValid();
      boolean hasInput = !tile.getSnapshotIn().isEmpty();
      boolean hasOutput = !tile.getSnapshotOut().isEmpty();
      int greenColour;
      int redColour;
      if (!valid) {
         greenColour = -14741477;
         redColour = -14540067;
      } else if (hasOutput) {
         greenColour = -8921737;
         redColour = COLOUR_RED_HALF;
      } else if (hasInput) {
         greenColour = -8921737;
         redColour = -14540067;
      } else {
         greenColour = -8921737;
         redColour = -14741477;
      }

      LedRenderUtil.setFacePosition(LED_GREEN, facing, 0.025, 0.15625, 0.21875);
      LedRenderUtil.setFacePosition(LED_RED, facing, 0.025, 0.28125, 0.21875);
      poseStack.pushPose();
      VertexConsumer led = buffers.getBuffer(BCLibRenderTypes.led());
      Pose pose = poseStack.last();
      LedRenderUtil.render(LED_GREEN, pose, led, skipFace, greenColour);
      LedRenderUtil.render(LED_RED, pose, led, skipFace, redColour);
      poseStack.popPose();
   }
}

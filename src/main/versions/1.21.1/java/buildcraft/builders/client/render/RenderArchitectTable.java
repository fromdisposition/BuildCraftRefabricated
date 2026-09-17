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

public class RenderArchitectTable implements BlockEntityRenderer<TileArchitectTable> {
   private static final RenderPartCube LED_ENERGY = new RenderPartCube();
   private static final RenderPartCube LED_STATE = new RenderPartCube();

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
      int energyColour = LedRenderUtil.COLOUR_OFF;
      int stateColour = LedRenderUtil.stateColour(tile.isScanning(), !tile.getIsValid());

      LedRenderUtil.setFacePosition(LED_ENERGY, facing, 0.025, 0.15625, 0.21875);
      LedRenderUtil.setFacePosition(LED_STATE, facing, 0.025, 0.28125, 0.21875);
      poseStack.pushPose();
      VertexConsumer led = buffers.getBuffer(BCLibRenderTypes.led());
      Pose pose = poseStack.last();
      LedRenderUtil.render(LED_ENERGY, pose, led, skipFace, energyColour);
      LedRenderUtil.render(LED_STATE, pose, led, skipFace, stateColour);
      poseStack.popPose();
   }
}

/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.client.render;

import buildcraft.builders.tile.TileArchitectTable;
import buildcraft.lib.client.render.tile.BcBlockEntityRenderer;
import buildcraft.lib.client.render.tile.LedRenderUtil;
import buildcraft.lib.client.render.tile.RenderPartCube;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;

public class RenderArchitectTable extends BcBlockEntityRenderer<TileArchitectTable, ArchitectTableRenderState> {
   private static final RenderPartCube LED_ENERGY = new RenderPartCube();
   private static final RenderPartCube LED_STATE = new RenderPartCube();

   public RenderArchitectTable(Context context) {
   }

   public ArchitectTableRenderState createRenderState() {
      return new ArchitectTableRenderState();
   }

   @Override
   protected void extract(TileArchitectTable tile, ArchitectTableRenderState state, float partialTick) {
      BlockState blockState = tile.getBlockState();
      if (blockState.hasProperty(HorizontalDirectionalBlock.FACING)) {
         state.facing = blockState.getValue(HorizontalDirectionalBlock.FACING);
         state.skipFace = state.facing.getOpposite();
         state.energyColour = LedRenderUtil.COLOUR_OFF;
         state.stateColour = LedRenderUtil.stateColour(tile.isScanning(), !tile.getIsValid());
      } else {
         state.facing = null;
      }
   }

   public void submit(ArchitectTableRenderState renderState, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
      if (renderState.facing != null) {
         Direction facing = renderState.facing;
         LedRenderUtil.setFacePosition(LED_ENERGY, facing, 0.025, 0.15625, 0.21875);
         LedRenderUtil.setFacePosition(LED_STATE, facing, 0.025, 0.28125, 0.21875);
         poseStack.pushPose();
         LedRenderUtil.submit(poseStack, collector, LED_ENERGY, renderState.skipFace, renderState.energyColour);
         LedRenderUtil.submit(poseStack, collector, LED_STATE, renderState.skipFace, renderState.stateColour);
         poseStack.popPose();
      }
   }
}

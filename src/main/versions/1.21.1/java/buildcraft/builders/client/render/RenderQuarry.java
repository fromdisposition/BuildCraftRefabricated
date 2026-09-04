/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.client.render;

import buildcraft.builders.tile.TileQuarry;
import buildcraft.lib.client.render.BCLibRenderTypes;
import buildcraft.lib.client.render.laser.LaserBatch;
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
import net.minecraft.world.phys.Vec3;

public class RenderQuarry implements BlockEntityRenderer<TileQuarry> {
   private static final RenderPartCube[] LED_ENERGY = new RenderPartCube[4];
   private static final RenderPartCube[] LED_STATE = new RenderPartCube[4];

   public RenderQuarry(BlockEntityRendererProvider.Context context) {
   }

   @Override
   public void render(TileQuarry tile, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
      BlockState blockState = tile.getBlockState();
      Direction front = blockState.hasProperty(HorizontalDirectionalBlock.FACING)
         ? blockState.getValue(HorizontalDirectionalBlock.FACING)
         : Direction.NORTH;
      Direction rear = front.getOpposite();
      boolean hasPower = tile.hasPower();
      boolean hasTask = tile.isMining();
      int energyColour = LedRenderUtil.energyColour((float)tile.getBattery().getStored() / tile.getBattery().getCapacity());
      int stateColour = LedRenderUtil.stateColour(hasPower && hasTask, !hasPower && hasTask);

      poseStack.pushPose();
      VertexConsumer led = buffers.getBuffer(BCLibRenderTypes.led());
      Pose pose = poseStack.last();
      for (int i = 0; i < 4; i++) {
         Direction dir = Direction.from2DDataValue(i);
         if (dir != rear) {
            Direction skipFace = dir.getOpposite();
            LedRenderUtil.render(LED_ENERGY[i], pose, led, skipFace, energyColour);
            LedRenderUtil.render(LED_STATE[i], pose, led, skipFace, stateColour);
         }
      }
      poseStack.popPose();

      // BE pass runs before the translucent water pass, so the drill lands in the depth buffer first and water blends over it.
      // cameraPos = this tile's BlockPos makes renderQuarry's world coords block-relative, matching this BER's already-translated pose.
      LaserBatch.begin();
      BCBuildersWorldRenderer.renderQuarry(tile, poseStack, Vec3.atLowerCornerOf(tile.getBlockPos()), partialTick);
      LaserBatch.end();
   }

   @Override
   public boolean shouldRenderOffScreen(TileQuarry tile) {
      // The frame/drill extends far beyond the quarry block, so don't frustum-cull the BER on the block alone.
      return true;
   }

   @Override
   public int getViewDistance() {
      // 256 covers a large quarry's frame/drill reach past the default 64-block BER view distance, so it doesn't vanish near the frame.
      return 256;
   }

   static {
      for (int i = 0; i < 4; i++) {
         Direction face = Direction.from2DDataValue(i);
         LED_ENERGY[i] = new RenderPartCube();
         LED_STATE[i] = new RenderPartCube();
         LedRenderUtil.setFacePosition(LED_ENERGY[i], face, 0.025, 0.09375, 0.84375);
         LedRenderUtil.setFacePosition(LED_STATE[i], face, 0.025, 0.21875, 0.84375);
      }
   }
}

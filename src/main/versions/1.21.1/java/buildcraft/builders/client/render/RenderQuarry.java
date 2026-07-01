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

/** 1.21.1 (versions/1.21.1) quarry renderer: immediate-mode status LEDs (skips the rear face). */
public class RenderQuarry implements BlockEntityRenderer<TileQuarry> {
   private static final RenderPartCube[] LED_GREEN = new RenderPartCube[4];
   private static final RenderPartCube[] LED_RED = new RenderPartCube[4];

   public RenderQuarry(BlockEntityRendererProvider.Context context) {
   }

   @Override
   public void render(TileQuarry tile, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
      BlockState blockState = tile.getBlockState();
      Direction front = blockState.hasProperty(HorizontalDirectionalBlock.FACING)
         ? (Direction) blockState.getValue(HorizontalDirectionalBlock.FACING)
         : Direction.NORTH;
      Direction rear = front.getOpposite();
      boolean hasPower = tile.hasPower();
      boolean hasTask = tile.isMining();
      boolean greenOn = hasPower || !hasTask;
      boolean redOn = !hasPower || !hasTask;
      int greenColour = greenOn ? -8921737 : -14741477;
      int redColour = redOn ? -14540067 : -14741477;

      poseStack.pushPose();
      VertexConsumer led = buffers.getBuffer(BCLibRenderTypes.led());
      Pose pose = poseStack.last();
      for (int i = 0; i < 4; i++) {
         Direction dir = Direction.from2DDataValue(i);
         if (dir != rear) {
            Direction skipFace = dir.getOpposite();
            LedRenderUtil.render(LED_GREEN[i], pose, led, skipFace, greenColour);
            LedRenderUtil.render(LED_RED[i], pose, led, skipFace, redColour);
         }
      }
      poseStack.popPose();

      // Frame + drill rendered HERE in the BER. The block-entity pass runs BEFORE the translucent water pass, so
      // (unlike the END_MAIN world-render path) the underwater drill lands in the depth buffer first and the
      // water then blends over it — visible through water from above, exactly like the LEDs above already are.
      // cameraPos = this quarry's own BlockPos makes renderQuarry's absolute world coords block-relative, which
      // matches this BER's pose (already translated to blockPos - camera). LaserBatch flushes inside the BE pass.
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
      // A large quarry's frame/drill reaches well past the default 64-block BER view distance; raise it (like a
      // beacon) so the render does not vanish when the camera is near the frame but far from the quarry block.
      return 256;
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

/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.client.render;

import buildcraft.builders.tile.TileQuarry;
import buildcraft.lib.client.render.tile.BcBlockEntityRenderer;
import buildcraft.lib.client.render.tile.LedRenderUtil;
import buildcraft.lib.client.render.tile.RenderPartCube;
//? if < 26.1 {
/*import buildcraft.lib.client.render.laser.LaserBatch;
import net.minecraft.world.phys.Vec3;
*///?}
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;

public class RenderQuarry extends BcBlockEntityRenderer<TileQuarry, QuarryRenderState> {
   private static final double LED_INSET = 0.025;
   private static final double ENERGY_OFFSET = 0.09375;
   private static final double STATE_OFFSET = 0.21875;
   private static final double Y = 0.84375;
   private static final RenderPartCube[] LED_ENERGY = new RenderPartCube[4];
   private static final RenderPartCube[] LED_STATE = new RenderPartCube[4];

   public RenderQuarry(Context context) {
   }

   public QuarryRenderState createRenderState() {
      return new QuarryRenderState();
   }

   @Override
   protected void extract(TileQuarry tile, QuarryRenderState state, float partialTick) {
      BlockState blockState = tile.getBlockState();
      Direction front = blockState.hasProperty(HorizontalDirectionalBlock.FACING)
         ? blockState.getValue(HorizontalDirectionalBlock.FACING)
         : Direction.NORTH;
      state.rear = front.getOpposite();
      boolean hasPower = tile.hasPower();
      boolean hasTask = tile.isMining();
      state.energyColour = LedRenderUtil.energyColour((float)tile.getBattery().getStored() / tile.getBattery().getCapacity());
      state.stateColour = LedRenderUtil.stateColour(hasPower && hasTask, !hasPower && hasTask);
   }

   public void submit(QuarryRenderState renderState, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
      poseStack.pushPose();

      for (int i = 0; i < 4; i++) {
         Direction dir = Direction.from2DDataValue(i);
         if (dir != renderState.rear) {
            Direction skipFace = dir.getOpposite();
            LedRenderUtil.submit(poseStack, collector, LED_ENERGY[i], skipFace, renderState.energyColour);
            LedRenderUtil.submit(poseStack, collector, LED_STATE[i], skipFace, renderState.stateColour);
         }
      }

      poseStack.popPose();

      //? if < 26.1 {
      /*// 1.21.10/1.21.11: the world-render path (BCBuildersWorldRenderer.renderAllQuarries) draws the frame/drill
      // via WorldRenderEvents AFTER translucent water, cutting the underwater drill from above. The block-entity
      // submit pass runs BEFORE translucent water, so draw the frame/drill here instead (immediate LaserBatch on
      // < 26.2), exactly like the LEDs above and the 1.21.1 immediate BER. cameraPos = this quarry's BlockPos makes
      // renderQuarry's absolute world coords block-relative, matching this BER's pose (translated to blockPos - cam).
      if (renderState.tile != null) {
         LaserBatch.begin();
         BCBuildersWorldRenderer.renderQuarry(
            renderState.tile, poseStack, Vec3.atLowerCornerOf(renderState.tile.getBlockPos()), renderState.partialTick
         );
         LaserBatch.end();
      }
      *///?}
   }

   //? if < 26.1 {
   /*// The frame/drill drawn in submit() extends far beyond the quarry block, so (like a beacon beam) the BER must
   // not be frustum-culled when the block leaves the view, and its view distance must be large enough that a big
   // quarry still renders when the camera is near the frame but far from the block. 26.1+ draws the frame/drill via
   // the world renderer (renderAllQuarries), which has no per-BER culling, so this is only needed for the BER path.
   @Override
   public boolean shouldRenderOffScreen() {
      return true;
   }

   @Override
   public int getViewDistance() {
      return 256;
   }
   *///?}

   static {
      for (int i = 0; i < 4; i++) {
         Direction face = Direction.from2DDataValue(i);
         LED_ENERGY[i] = new RenderPartCube();
         LED_STATE[i] = new RenderPartCube();
         LedRenderUtil.setFacePosition(LED_ENERGY[i], face, LED_INSET, ENERGY_OFFSET, Y);
         LedRenderUtil.setFacePosition(LED_STATE[i], face, LED_INSET, STATE_OFFSET, Y);
      }
   }
}

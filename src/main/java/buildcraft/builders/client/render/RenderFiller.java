/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.client.render;

import buildcraft.api.tiles.IControllable;
import buildcraft.builders.tile.TileFiller;
import buildcraft.lib.client.render.tile.BcBlockEntityRenderer;
import buildcraft.lib.client.render.tile.LedRenderUtil;
import buildcraft.lib.client.render.tile.RenderPartCube;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;

public class RenderFiller extends BcBlockEntityRenderer<TileFiller, FillerRenderState> {
   private static final double LED_INSET = 0.025;
   private static final double ENERGY_OFFSET = 0.09375;
   private static final double STATE_OFFSET = 0.21875;
   private static final double Y = 0.84375;
   private static final RenderPartCube[] LED_ENERGY = new RenderPartCube[4];
   private static final RenderPartCube[] LED_STATE = new RenderPartCube[4];

   public RenderFiller(Context context) {
   }

   public FillerRenderState createRenderState() {
      return new FillerRenderState();
   }

   @Override
   protected void extract(TileFiller tile, FillerRenderState state, float partialTick) {
      boolean active = tile.getControlMode() != IControllable.Mode.OFF && !tile.isFinished();
      boolean ready = tile.hasPower() && tile.isValid();
      state.energyColour = LedRenderUtil.energyColour((float)tile.getBattery().getStored() / tile.getBattery().getCapacity());
      state.stateColour = LedRenderUtil.stateColour(active && ready, active && !ready);
   }

   public void submit(FillerRenderState renderState, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
      poseStack.pushPose();

      for (int i = 0; i < 4; i++) {
         Direction skipFace = Direction.from2DDataValue(i).getOpposite();
         LedRenderUtil.submit(poseStack, collector, LED_ENERGY[i], skipFace, renderState.energyColour);
         LedRenderUtil.submit(poseStack, collector, LED_STATE[i], skipFace, renderState.stateColour);
      }

      poseStack.popPose();
   }

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

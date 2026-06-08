/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.render;

import buildcraft.api.transport.pipe.IPipeFlowRenderer;
import buildcraft.transport.pipe.flow.PipeFlowRedstoneFlux;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;

public enum PipeFlowRendererFE implements IPipeFlowRenderer<PipeFlowRedstoneFlux> {
   INSTANCE;

   public void render(PipeFlowRedstoneFlux flow, double x, double y, double z, float partialTicks, VertexConsumer bb, Pose pose) {
      PipeFlowRendererEnergy.render(
         flow.pipe,
         flow.getSections(),
         flow.clientDisplayFlowCentreLast,
         flow.clientDisplayFlowCentre,
         true,
         partialTicks,
         bb,
         pose,
         PipeRenderContext.getPackedLight()
      );
   }
}

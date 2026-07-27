/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.render;

import buildcraft.api.transport.pipe.IPipeFlowRenderer;
import buildcraft.transport.pipe.flow.PipeFlowPower;
import buildcraft.transport.pipe.flow.PipeFlowRedstoneFlux;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;

public final class PipeFlowRendererEnergyAdapter {
   public static final IPipeFlowRenderer<PipeFlowPower> POWER = new PipeFlowRendererEnergyAdapter.PowerRenderer();
   public static final IPipeFlowRenderer<PipeFlowRedstoneFlux> FE = new PipeFlowRendererEnergyAdapter.FeRenderer();

   private PipeFlowRendererEnergyAdapter() {
   }

   private static final class PowerRenderer implements IPipeFlowRenderer<PipeFlowPower> {
      public void render(PipeFlowPower flow, double x, double y, double z, float partialTicks, VertexConsumer buffer, Pose pose) {
         PipeFlowRendererEnergy.render(
            flow.pipe,
            flow.getSections(),
            flow.clientDisplayFlowCentreLast,
            flow.clientDisplayFlowCentre,
            false,
            partialTicks,
            buffer,
            pose,
            PipeRenderContext.getPackedLight()
         );
      }
   }

   private static final class FeRenderer implements IPipeFlowRenderer<PipeFlowRedstoneFlux> {
      public void render(PipeFlowRedstoneFlux flow, double x, double y, double z, float partialTicks, VertexConsumer buffer, Pose pose) {
         PipeFlowRendererEnergy.render(
            flow.pipe,
            flow.getSections(),
            flow.clientDisplayFlowCentreLast,
            flow.clientDisplayFlowCentre,
            true,
            partialTicks,
            buffer,
            pose,
            PipeRenderContext.getPackedLight()
         );
      }
   }
}

package buildcraft.transport.client.render;

import buildcraft.api.transport.pipe.IPipeFlowRenderer;
import buildcraft.transport.pipe.flow.PipeFlowPower;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;

public enum PipeFlowRendererPower implements IPipeFlowRenderer<PipeFlowPower> {
   INSTANCE;

   public void render(PipeFlowPower flow, double x, double y, double z, float partialTicks, VertexConsumer bb, Pose pose) {
      PipeFlowRendererEnergy.render(
         flow.pipe,
         flow.getSections(),
         flow.clientDisplayFlowCentreLast,
         flow.clientDisplayFlowCentre,
         false,
         partialTicks,
         bb,
         pose,
         PipeRenderContext.getPackedLight()
      );
   }
}

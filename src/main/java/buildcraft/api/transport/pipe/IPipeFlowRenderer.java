package buildcraft.api.transport.pipe;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;

public interface IPipeFlowRenderer<F extends PipeFlow> {
   default void render(F flow, double x, double y, double z, float partialTicks, VertexConsumer bufferBuilder, PoseStack poseStack) {
      this.render(flow, x, y, z, partialTicks, bufferBuilder, poseStack.last());
   }

   default void render(F flow, double x, double y, double z, float partialTicks, VertexConsumer bufferBuilder, Pose pose) {
      this.render(flow, x, y, z, partialTicks, bufferBuilder);
   }

   @Deprecated
   default void render(F flow, double x, double y, double z, float partialTicks, VertexConsumer bufferBuilder) {
   }
}

package buildcraft.api.transport.pipe;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

public interface IPipeBehaviourRenderer<B extends PipeBehaviour> {
    void render(B behaviour, double x, double y, double z, float partialTicks, VertexConsumer bb, PoseStack.Pose pose);
}
